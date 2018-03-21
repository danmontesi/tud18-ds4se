import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.concurrent.ConcurrentHashMap;

public class SizeVisitor implements CommitVisitor {

    private ConcurrentHashMap<String, Integer> sizeMap;

    public SizeVisitor(ConcurrentHashMap<String,Integer> sizeMap) {
        this.sizeMap = sizeMap;
    }

    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
        for (Modification m : commit.getModifications()) {
            String filename = m.getNewPath();
            String oldFilename = m.getOldPath();
            int size = m.getSourceCode().split("\n").length;

            if(filename == null || !filename.equals(oldFilename)) {
                if(filename == null) {
                    sizeMap.remove(oldFilename);
                }
                else {
                    sizeMap.put(filename, size);
                    sizeMap.remove(oldFilename);
                }
            }  else {
                sizeMap.put(filename, size);
            }
        }
    }

    public ConcurrentHashMap<String,Integer> getSizeMap() {
        return sizeMap;
    }
}
