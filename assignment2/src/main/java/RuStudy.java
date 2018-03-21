import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;

public class RuStudy implements Study {

    public static void main(String[] args) {
        new RepoDriller().start(new RuStudy());
    }


    public void execute() {
        ConcurrentHashMap<String,Integer> defectsMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> churnMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> sizeMap  = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap = new ConcurrentHashMap<>();

        BugVisitor bugVisitor = new BugVisitor(new GregorianCalendar(2016,0,1),new GregorianCalendar(2016,6,1), defectsMap);
        ChurnVisitor churnVisitor = new ChurnVisitor(churnMap);
        SizeVisitor sizeVisitor = new SizeVisitor(sizeMap);
        OwnerVisitor ownerVisitor = new OwnerVisitor(ownerMap);

        // owner visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.since(new GregorianCalendar(2016,0,0)))
                .process(ownerVisitor, new CSVFile("/tmp/output.csv"))
                .mine();

        // churn visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.since(new GregorianCalendar(2016,0,0)))
                .process(churnVisitor, new CSVFile("/tmp/output.csv"))
                .mine();


        // size visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.since(new GregorianCalendar(2016,0,0)))
                .process(sizeVisitor, new CSVFile("/tmp/output.csv"))
                .mine();


        // bug visitor
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                .through(Commits.since(new GregorianCalendar(2016,0,0)))
                .process(bugVisitor, new CSVFile("/tmp/output.csv"))
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
