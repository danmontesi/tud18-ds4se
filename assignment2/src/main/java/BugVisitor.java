import org.repodriller.domain.*;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.BlamedLine;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BugVisitor implements CommitVisitor {

    private Calendar notBefore;
    private Calendar notAfter;
    private ConcurrentHashMap<String,Integer> defectsMap;

    private static Set<String> exclusions = new HashSet<>();

    public BugVisitor(Calendar notBefore, Calendar notAfter, ConcurrentHashMap<String,Integer> defectsMap) {
        this.notBefore  = notBefore;
        this.notAfter = notAfter;
        this.defectsMap = defectsMap;
    }

    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
        if(!isBugFixCommit(commit)) {
            return;
        }

        outer: for(Modification m : commit.getModifications()) {
            if(m.getAdded() + m.getRemoved() > 50) {
                System.out.println("skipping huge diff");
                continue;
            }

            if(m.getType() == ModificationType.MODIFY) {
                List<Integer> cleanLines = new ArrayList<>();
                List<List<DiffLine>> linesToBlame = linesToBlame(m.getDiff());

                for (List<DiffLine> linesInThisFragment: linesToBlame) {
                    for(DiffLine line : linesInThisFragment) {
                        if(line.getLine().trim().startsWith("/") || line.getLine().trim().startsWith("*")) {
                            // comment, ignore
                        } else if(line.getType() == DiffLineType.ADDED) {
                            // added, ignore
                        }
                        else {
                            cleanLines.add(line.getLineNumber());
                        }
                    }
                }

                try {
                    // what about added / removed etc
                    List<BlamedLine> blamedLines = repo.getScm().blame(m.getOldPath(), commit.getHash(), true);

                    for (BlamedLine blamed : blamedLines) {
                        if(!cleanLines.contains(blamed.getLineNumber())) {
                            continue;
                        }

                        String commitId = blamed.getCommit();

                        if(exclusions.contains(commitId)) {
                            System.out.print(".");
                            continue;
                        }

                        Calendar date = repo.getScm().getCommit(commitId).getDate();

                        if (date.before(notBefore) || date.after(notAfter)) {
                            exclusions.add(commitId);
                            System.out.println("ignoring bugfix (not in the relevant time period)");
                        } else {
                            System.out.println("bingo");
                            int defectsNumber = defectsMap.getOrDefault(m.getFileName(), 0);
                            defectsMap.put(m.getFileName(), defectsNumber);
                            continue outer;
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println("this never happens (most of the time)");
                }
            }
        }
    }

    private boolean isBugFixCommit(Commit commit) {
        String message = commit.getMsg();
        return (message.contains("fix") || message.contains("bug") || message.contains("repair")
                || message.contains("resolves") || message.contains("correct"));
    }


    private List<List<DiffLine>> linesToBlame(String diff) {
        DiffParser parsedDiff = new DiffParser(diff);
        List<List<DiffLine>> lines = new ArrayList<>();

        for(int i=0;i < parsedDiff.getBlocks().size(); i++) {
            List<DiffLine> oldLines = parsedDiff.getBlocks().get(i).getLinesInOldFile();
            lines.add(oldLines);
        }

        return lines;
    }
}