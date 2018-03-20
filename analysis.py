from git import Repo, Tree, Blob, Submodule



def run():
    # open repository
    repo = Repo("../rust")

    results = {}
    visited = {}

    to_visit = [repo.tag("refs/tags/1.14.0").commit]
    while len(to_visit) > 0:
        handle_commit(to_visit.pop(), results, visited, to_visit)
    print(results)


def handle_commit(commit, results, visited, to_visit):
    blobs = 0

    visited[commit.binsha] = True

    for parent in commit.parents:
        for x in commit.diff(parent):
            if x.b_path not in results:
                results[x.b_path] = {}

            if commit.author not in results[x.b_path]:
                results[x.b_path][commit.author] = 0

            results[x.b_path][commit.author] += 1

        if parent.binsha not in visited:
            to_visit.append(parent)

if __name__ == "__main__":
    run()
