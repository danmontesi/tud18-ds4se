import json
import os.path
from git import Repo
import csv

THRESHOLD_MINOR = 0.05
REPO_PATH = "../rust"
INTERMEDIATE_RES_PATH = "results.json"
FINAL_RES_PATH = "results.tsv"
TAG = "refs/tags/1.14.0"


def main():
    """performs the analysis if the INTERMEDIATE_RES_PATH does not exist yet and creates the result.tsv as output"""
    if not os.path.isfile(INTERMEDIATE_RES_PATH):
        run_analysis()

    with open(INTERMEDIATE_RES_PATH) as fp:
        results = json.load(fp)

    calculate_features(results)


def calculate_features(results):
    """creates the FINAL_RES_PATH based on the dict<filename,dict<author, #commits by author>> of analysis
    results it is given"""

    with open(FINAL_RES_PATH, "w") as file:
        writer = csv.writer(file, delimiter="\t")
        writer.writerow(("filename", "minor", "major", "total", "ownership"))

        # Iterate over files
        for (filename, authors) in results.items():

            num_authors = len(authors)

            num_commits = 0
            for (author, author_commits) in authors.items():
                num_commits += author_commits

            num_max_commits = 0
            num_minor_contributors = 0

            for (author, author_commits) in authors.items():
                if author_commits > num_max_commits:
                    num_max_commits = author_commits

                if author_commits / (num_commits * 1.0) <= THRESHOLD_MINOR:
                    num_minor_contributors += 1

            writer.writerow((filename, str(num_minor_contributors), str(num_authors - num_minor_contributors),
                            str(num_authors), str(num_max_commits / num_commits * 1.0)))


def run_analysis():
    """Crawl the repoistory and build data structure for number of changes per file per author.
    Saves json to INTERMEDIATE_RES_PATH (dict<filename,dict<author, #commits by author>>)"""
    repo = Repo(REPO_PATH)

    results = {}
    visited = {}

    cnt = 0

    to_visit = [repo.tag(TAG).commit]
    while len(to_visit) > 0:
        print(cnt)
        cnt += 1
        handle_commit(to_visit.pop(), results, visited, to_visit)

    with open(INTERMEDIATE_RES_PATH, "w") as file:
        file.write(json.dumps(results))


def handle_commit(commit, results, visited, to_visit):
    """Add the data from one commit to the data structure and add any unvisited non-pending parents to the list of
    nodes to explore."""
    visited[commit.binsha] = True

    for parent in commit.parents:
        for diff in commit.diff(parent):

            # make sure dict exists at both levels
            if diff.b_path not in results:
                results[diff.b_path] = {}
            if commit.author.email not in results[diff.b_path]:
                results[diff.b_path][commit.author.email] = 0

            results[diff.b_path][commit.author.email] += 1

        if parent.binsha not in visited and parent.binsha not in to_visit:
            to_visit.append(parent)


if __name__ == "__main__":
    main()
