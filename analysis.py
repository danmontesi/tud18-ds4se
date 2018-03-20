import json
import os.path
from git import Repo, Tree, Blob, Submodule


def run():
    if not os.path.isfile('results.json'):
        run_analysis()

    with open('results.json') as fp:
        results = json.load(fp)

    calculate_features(results)


def calculate_features(results):
    res_string = "filename \t minor \t major \t total \t ownership\n"

    for (filename,repo_file) in results.items():
        res_string += filename + "\t"

        total = len(repo_file)

        sum = 0
        for (_, value) in repo_file.items():
            sum += value

        best = 0
        minor = 0

        for (_,value) in repo_file.items():
            if value > best:
                best = value

            if value/(sum*1.0) <= 0.05:
                minor += 1

        res_string += str(minor) + "\t"
        res_string += str(total-minor) + "\t"
        res_string += str(total) + "\t"
        res_string += str(best/sum*1.0) + "\n"

    with open('results.tsv','w') as file:
        file.write(res_string)


def run_analysis():
    # open repository
    repo = Repo("../rust")

    results = {}
    visited = {}

    cnt = 0

    to_visit = [repo.tag("refs/tags/1.14.0").commit]
    while len(to_visit) > 0:
        print(cnt)
        cnt += 1
        handle_commit(to_visit.pop(), results, visited, to_visit)

    with open('results.json','w') as file:
        file.write(json.dumps(results))


def handle_commit(commit, results, visited, to_visit):
    blobs = 0

    visited[commit.binsha] = True

    for parent in commit.parents:
        for x in commit.diff(parent):
            if x.b_path not in results:
                results[x.b_path] = {}

            if commit.author.email not in results[x.b_path]:
                results[x.b_path][commit.author.email] = 0

            results[x.b_path][commit.author.email] += 1

        if parent.binsha not in visited:
            to_visit.append(parent)


if __name__ == "__main__":
    run()
