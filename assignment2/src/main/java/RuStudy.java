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

    public static String earliestCommit = "e8a0123241f0d397d39cd18fcc4e5e7edde22730";
    public static String commitAtRelease = "3d7cd77e442ce34eaac8a176ae8be17669498ebc";
    public static String commitYearAfterRelease = "766bd11c8a3c019ca53febdcd77b2215379dd67d";

    public static Calendar noBugsIntroducedBefore =  new GregorianCalendar(2016,11,22);
    public static Calendar noBugsIntroducedAfter = new GregorianCalendar(2017,6,20)

    public static void main(String[] args) {
        new RepoDriller().start(new RuStudy());
    }


    public void execute() {
        ConcurrentHashMap<String ,Integer> defectsMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> churnMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> sizeMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap = new ConcurrentHashMap<>();

        BugVisitor bugVisitor = new BugVisitor(noBugsIntroducedBefore, noBugsIntroducedAfter, defectsMap);
        ChurnVisitor churnVisitor = new ChurnVisitor(churnMap);
        SizeVisitor sizeVisitor = new SizeVisitor(sizeMap);
        OwnerVisitor ownerVisitor = new OwnerVisitor(ownerMap);

        // churn, owner, size visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.range(earliestCommit, commitAtRelease))
                .process(ownerVisitor).process(churnVisitor).process(sizeVisitor)
                .mine();


        // bug visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.range(commitAtRelease, commitYearAfterRelease))
                .process(bugVisitor)
                .mine();

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
