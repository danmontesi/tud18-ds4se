import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

public class RuStudy implements Study {

    public static void main(String[] args) {
        new RepoDriller().start(new RuStudy());
    }


    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject("/Users/danmontesi/Desktop/rust"))
                .through(Commits.range("9b21dcd6a89f38e8ceccb2ede8c9027cb409f6e3","738e30eaea16bf68d27c91567c8fe13b2057d1cf"))
                .process(new MinorCommitter(), new CSVFile("/Users/danmontesi/Desktop/tud18-ds4se/assignment2/output.csv"))
                .mine();
    }


}
