import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.scm.GitRepository;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;

public class RuStudy implements Study {

    private static String repoPath = "/u/halle/obers/home_at/temp2/gimp";

    private static String earliestCommit = "92f2b0092925a52e8ea9436a2e2745bd32edaa54"; // nov 25 2015
    private static String commitAtRelease = "91b41bba51af9635b97847ee307e86c220ee0657"; // feb 1 2017
    private static String commitYearAfterRelease = "18794a6ba2915ed58b82337edaba794d69f767b7"; // dec 12 2017

    private static Calendar noBugsIntroducedBefore =  new GregorianCalendar(2017,1,1);
    private static Calendar noBugsIntroducedAfter = new GregorianCalendar(2017,6,13);

    public void execute() {
        ConcurrentHashMap<String ,Integer> defectsMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> churnMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> sizeMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap = new ConcurrentHashMap<>();

        BugVisitor bugVisitor = new BugVisitor(noBugsIntroducedBefore, noBugsIntroducedAfter, defectsMap);
        ChurnVisitor churnVisitor = new ChurnVisitor(churnMap);
        SizeVisitor sizeVisitor = new SizeVisitor(sizeMap);
        OwnerVisitor ownerVisitor = new OwnerVisitor(ownerMap);

        BugAnalysis ba = new BugAnalysis(repoPath, defectsMap, commitAtRelease, commitYearAfterRelease,
                noBugsIntroducedBefore, noBugsIntroducedAfter);
        ba.start();

        // churn, owner, size visitor
        new RepositoryMining()
                .in(GitRepository.singleProject(repoPath))
                .through(Commits.range(earliestCommit, commitAtRelease))
                .process(ownerVisitor).process(churnVisitor).process(sizeVisitor)
                .mine();

        try {
            ba.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OwnershipCalculator oc = new OwnershipCalculator(ownerMap);

        ConcurrentHashMap<String, Integer> minorMap = oc.getMinorMap();
        ConcurrentHashMap<String, Integer> majorMap = oc.getMajorMap();
        ConcurrentHashMap<String, Double> ownershipMap = oc.getOwnershipMap();

        try {
            TSVCreator.write("results.tsv", churnMap, sizeMap, defectsMap, minorMap, majorMap, ownershipMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
