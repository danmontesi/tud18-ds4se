import org.repodriller.domain.*;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.BlamedLine;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BugVisitor implements CommitVisitor {
    /**
     * Cutoff date for the introduction date of the bug (if it was introduced prior to this, we don't consider
     * it a post-release bug)
     */
    private Calendar notBefore;

    /**
     * Cutoff date for the introduction date of the bug (if it was introduced after this, we don't consider
     * it a post-release bug
     */
    private Calendar notAfter;

    /**
     * Map containing all the bugs
     */
    private ConcurrentHashMap<String,Integer> defectsMap;

    /**
     * Set of the hashes we know to violate the time constraints, used for speedup
     */
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

        Calendar dateCommit = commit.getDate();
        if (dateCommit.before(notBefore)) {
            System.out.println("ignoring bugfix (not in the relevant time period / other branch)");
            return;
        }

        Collection<Modification> mods = commit.getModifications();

        if(mods.size() > 10) {
            System.out.println("ignoring bugfix (too many files)");
            return;
        }

        outer: for(Modification m : mods) {
            if(m.getAdded() + m.getRemoved() > 50) {
                System.out.println("ignoring huge diff");
                continue;
            }

            if(m.getType() != ModificationType.MODIFY) {
                // Adding and deletion are not relevant for bugfixes
                continue;
            }

            List<Integer> relevantLines = new ArrayList<>();
            List<List<DiffLine>> linesToBlame = linesToBlame(m.getDiff());

            // go through all changes and calculate the lines we need to blame
            for (List<DiffLine> linesInThisFragment: linesToBlame) {
                for(DiffLine line : linesInThisFragment) {
                    if(line.getLine().trim().startsWith("/") || line.getLine().trim().startsWith("*")) {
                        // comment, ignore
                        continue;
                    } else if(line.getType() == DiffLineType.ADDED) {
                        // added line, we can not trace back the commit that caused the issue here
                        continue;
                    }
                    else {
                        relevantLines.add(line.getLineNumber());
                    }
                }
            }

            try {
                List<BlamedLine> blamedLines = repo.getScm().blame(m.getOldPath(), commit.getHash(), true);

                for(int i:relevantLines) {
                    BlamedLine blamed = blamedLines.get(i-1); // -1 to account for 0 based index
                    String commitId = blamed.getCommit();

                    if(exclusions.contains(commitId)) {
                        System.out.println("ignoring bugfix (not in the relevant time period) [cache]");
                        continue;
                    }

                    // get date of the commit that introduced the issue
                    Calendar date = repo.getScm().getCommit(commitId).getDate();

                    if (date.before(notBefore) || date.after(notAfter)) {
                        exclusions.add(commitId);
                        System.out.println("ignoring bugfix (not in the relevant time period)");
                    } else {
                        System.out.println("detected relevant bugfix");
                        int defectsNumber = defectsMap.getOrDefault(m.getOldPath(), 0);
                        if(!m.getOldPath().equals(m.getNewPath())) {
                            defectsNumber += defectsMap.getOrDefault(m.getNewPath(), 0);
                        }
                        defectsMap.put(m.getNewPath(), defectsNumber+1);
                        continue outer; // to avoid identifying multiple defects in the same file
                    }
                }
            }
            catch(Exception e) {
                System.out.println("Exception while processing diff: " + e.toString());
            }
        }
    }

    /**
     * Is a specific commit a bug fix? Identified by looking for words such as bug or fix in the commit message
     * @param commit commit to be analyzed
     * @return true if commit is considered to fix a bug, false otherwise
     */
    private static boolean isBugFixCommit(Commit commit) {
        String message = commit.getMsg().toLowerCase();
        boolean knockout = (message.contains("fix") || message.contains("bug") || message.contains("repair")
                || message.contains("resolve") || message.contains("correct") || message.contains("error")
                || message.contains("crash"));

        return knockout && (!message.contains("test") || message.contains("fix"));
    }

    /**
     * Starting from a string diff, get the DiffLines in the old file that need to be analyzed
     * @param diff the output from git diff
     * @return diffLines that need to be looked at
     */
    private static List<List<DiffLine>> linesToBlame(String diff) {
        DiffParser parsedDiff = new DiffParser(diff);
        List<List<DiffLine>> lines = new ArrayList<>();

        for(int i=0;i < parsedDiff.getBlocks().size(); i++) {
            List<DiffLine> oldLines = parsedDiff.getBlocks().get(i).getLinesInOldFile();
            lines.add(oldLines);
        }

        return lines;
    }
}