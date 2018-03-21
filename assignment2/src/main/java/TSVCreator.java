import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class TSVCreator {

    public static void write(String filename,
        ConcurrentHashMap<String, Integer> churnMap,
        ConcurrentHashMap<String, Integer> sizeMap,
        ConcurrentHashMap<String, Integer> defectsMap,
        ConcurrentHashMap<String, Integer> minorMap,
        ConcurrentHashMap<String, Integer> majorMap,
        ConcurrentHashMap<String, Double> ownershipMap) throws IOException {

            StringBuilder sb = new StringBuilder();
            sb.append("filename\tchurn\tsize\tminor\tmajor\ttotal\townership\tdefects\n");

            for(String key : churnMap.keySet()) {
                sb.append(key);
                sb.append("\t");
                sb.append(churnMap.get(key));
                sb.append("\t");
                sb.append(sizeMap.get(key));
                sb.append("\t");
                sb.append(minorMap.get(key));
                sb.append("\t");
                sb.append(majorMap.get(key));
                sb.append("\t");
                sb.append(minorMap.get(key)+majorMap.get(key));
                sb.append("\t");
                sb.append(ownershipMap.get(key));
                sb.append("\t");
                sb.append(defectsMap.get(key));
                sb.append("\n");
            }

            for(String key : defectsMap.keySet()) {
                sb.append(key);
                sb.append("\t");
                sb.append(defectsMap.get(key));
                sb.append("\n");
            }

            File file = new File(filename);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(sb.toString());
            }
    }
}
