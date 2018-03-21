import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.concurrent.ConcurrentHashMap;

public class OwnerVisitor implements CommitVisitor {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap;

    public OwnerVisitor(ConcurrentHashMap<String,ConcurrentHashMap<String, Integer>> ownerMap) {
        this.ownerMap = ownerMap;
    }

    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {

        for (Modification m : commit.getModifications()) {
            String filename = m.getNewPath();
            String oldFilename = m.getOldPath();


            if(filename == null || !filename.equals(oldFilename)) {
                if(filename == null) {
                    ownerMap.remove(oldFilename);
                }
                else {
                    ownerMap.put(filename, ownerMap.getOrDefault(oldFilename, new ConcurrentHashMap<>()));
                    ownerMap.remove(oldFilename);

                    int previousCommits = ownerMap.get(filename).getOrDefault(commit.getAuthor().toString(),0);
                    ownerMap.get(filename).put(commit.getAuthor().toString(),previousCommits+1);
                }
            }  else {
                int previousCommits = 0;
                if(ownerMap.get(filename) != null) {
                    previousCommits = ownerMap.get(filename).getOrDefault(commit.getAuthor().toString(),0);
                }
                ownerMap.get(filename).put(commit.getAuthor().toString(),previousCommits+1);
            }
            System.out.println("one down");
        }
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> getOwnerMap() {
        return ownerMap;
    }
}
