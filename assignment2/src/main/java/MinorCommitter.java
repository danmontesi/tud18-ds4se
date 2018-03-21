import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class  MinorCommitter implements CommitVisitor {

    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        System.out.println("okkk");
        writer.write(
                commit.getHash(),
                commit.getCommitter().getName());
        System.out.println("ok");
    }

    public String name(){
        return "commits";
    }
}
