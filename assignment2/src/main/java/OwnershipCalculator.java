import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to calculate minor, major and ownership based on a HashMap of files to a hashmap of developers
 * to a hashmap of contributions by them
 */
public class OwnershipCalculator {

    private ConcurrentHashMap<String, Integer> minorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> majorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Double> ownershipMap = new ConcurrentHashMap<>();

    /**
     * Calculate the ownership information. The individual HashMap s will be available via the getters
     * @param ownerMap HashMap<fileName,HashMap<developerName>>
     */
    public OwnershipCalculator(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap) {
        for(String file: ownerMap.keySet()) {
            int commitNumber = 0;
            int highestCommitNumber = 0;

            // Add up number of commits total and determine highest number of contributions by a single author
            for(Integer commits:ownerMap.get(file).values()) {
                commitNumber += commits;

                if(highestCommitNumber < commits) {
                    highestCommitNumber = commits;
                }
            }

            double ownership = highestCommitNumber/(commitNumber*1.0);
            int major = 0;
            int minor = 0;

            for(Integer commits:ownerMap.get(file).values()) {
                if(commits/(commitNumber*1.0) > 0.05) {
                    major++;
                } else {
                    minor++;
                }
            }

            minorMap.put(file, minor);
            majorMap.put(file, major);
            ownershipMap.put(file, ownership);
        }

    }

    public ConcurrentHashMap<String, Integer> getMinorMap() {
        return minorMap;
    }

    public ConcurrentHashMap<String, Integer> getMajorMap() {
        return majorMap;
    }

    public ConcurrentHashMap<String, Double> getOwnershipMap() {
        return ownershipMap;
    }
}
