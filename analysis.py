from git import Repo, Tree, Blob, Submodule


def run():
    # open repository
    repo = Repo("../rust")
    handle_commit(repo.tag("refs/tags/1.14.0").commit)
    count = 0



def handle_commit(commit):
    blobs = 0

    # for i in commit.tree.traverse():
    for i in commit.:
        if isinstance(i, Tree):
            print("tree")
            # ignore for now
        elif isinstance(i, Submodule):
            # we are not considering submoules to be part of this software product
            pass
        else:
            print(i.path)
            print(type(i))
            assert isinstance(i, Blob)
            blobs += 1

    print(blobs)

if __name__ == "__main__":
    run()
