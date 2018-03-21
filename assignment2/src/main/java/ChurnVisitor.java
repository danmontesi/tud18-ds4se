import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.concurrent.ConcurrentHashMap;

public class ChurnVisitor implements CommitVisitor {

    private ConcurrentHashMap<String, Integer> churnMap;

    public ChurnVisitor(ConcurrentHashMap<String,Integer> churnMap) {
        this.churnMap = churnMap;
    }

    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {

        for (Modification m : commit.getModifications()) {
            String filename = m.getNewPath();
            String oldFilename = m.getOldPath();

            if(filename == null || !filename.equals(oldFilename)) {
                if(filename == null) {
                    churnMap.remove(oldFilename);
                }
                else {
                    int churn = m.getAdded() + m.getRemoved() + churnMap.getOrDefault(oldFilename, 0);
                    churnMap.put(filename, churn);
                    churnMap.remove(oldFilename);
                }
            }  else {
                int churn = m.getAdded() + m.getRemoved() + churnMap.getOrDefault(filename, 0);
                churnMap.put(filename, churn);
            }
        }
    }

    public ConcurrentHashMap<String,Integer> getChurnMap() {
        return churnMap;
    }
}
