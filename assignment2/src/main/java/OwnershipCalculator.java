import java.util.concurrent.ConcurrentHashMap;

public class OwnershipCalculator {

    private ConcurrentHashMap<String, Integer> minorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> majorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Double> ownershipMap = new ConcurrentHashMap<>();

    public OwnershipCalculator(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> ownerMap) {
        for(String file: ownerMap.keySet()) {
            int commitNumber = 0;
            int highestCommitNumber = 0;

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
