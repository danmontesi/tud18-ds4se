import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.scm.GitRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RuStudy implements Study {

    public static void main(String[] args) {
        new RepoDriller().start(new RuStudy());
    }



    public void execute() {

        ConcurrentHashMap<String,Integer> sizeMap = new ConcurrentHashMap<>();

            new RepositoryMining()
                    .in(GitRepository.singleProject("/home/michael/Documents/athens/rust-repo/rust"))
                    .through(Commits.all())
                    .process(new SizeVisitor(sizeMap))
                    .mine();

            for(Map.Entry<String,Integer> pair: sizeMap.entrySet()) {
                System.out.println(pair.getKey() + " " + pair.getValue());
            }

    }

}


