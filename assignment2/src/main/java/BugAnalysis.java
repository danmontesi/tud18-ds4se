import org.repodriller.RepositoryMining;
import org.repodriller.filter.range.Commits;
import org.repodriller.scm.GitRepository;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

public class BugAnalysis extends  Thread {
    private static String commitAtRelease;
    private static String commitYearAfterRelease;
    private static String repoPath;

    private static Calendar noBugsIntroducedBefore;
    private static Calendar noBugsIntroducedAfter;

    ConcurrentHashMap<String, Integer> defectsMap;

    public BugAnalysis(String repoPath, ConcurrentHashMap<String, Integer> defectsMap, String commitAtRelease,
                       String commitYearAfterRelease, Calendar noBugsIntroducedBefore, Calendar noBugsIntroducedAfter) {
        this.defectsMap = defectsMap;
        this.commitAtRelease = commitAtRelease;
        this.commitYearAfterRelease = commitYearAfterRelease;
        this.noBugsIntroducedBefore = noBugsIntroducedBefore;
        this.noBugsIntroducedAfter = noBugsIntroducedAfter;
        this.repoPath = repoPath;
    }

    public void run() {

        BugVisitor bugVisitor = new BugVisitor(noBugsIntroducedBefore, noBugsIntroducedAfter, defectsMap);
        // bug visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.range(commitAtRelease, commitYearAfterRelease))
                .process(bugVisitor)
                .mine();
    }
}
