import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowLogProcessor {

    public static Map<String, String> parseLookupTable(String filePath) throws IOException {
        Map<String, String> lookup = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        //lookup table is formed using below two links
        //https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
        //https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html

        // Skip the header line
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length >= 3) {
                String port = parts[0].trim();
                String protocol = parts[1].trim().toLowerCase(); // Normalize protocol to lowercase
                String tag = parts[2].trim();
                lookup.put(port + "," + protocol, tag);
            }
        }
        return lookup;
    }

    public static List<String[]> parseFlowLogs(String filePath) throws IOException {
        List<String[]> logs = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            String[] parts = line.split("\\s+");

            //https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html
            /*
            According to the above link, columns in the log file are 
             0.version
             1.account_id
             2.interface_id
             3.srcaddr
             4.dstaddr
             5.srcport
             6.protocol and son on
            */
            if (parts.length >= 14 && parts[0].equals("2")) {
                String dstport = parts[6].trim();
                String protocolNumber = parts[7].trim();
                String protocol = "";

                // Map protocol numbers to names
                //Note: https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml gives us the mapping between protocol names and numbers
                if (protocolNumber.equals("6")) {
                    protocol = "tcp";
                } else if (protocolNumber.equals("17")) {
                    protocol = "udp";
                }

                logs.add(new String[]{dstport, protocol});
            }
        }
        return logs;
    }

    // Method to process logs and match them to tags
    public static Map<String, Object> processLogs(List<String[]> logs, Map<String, String> lookup) {
        Map<String, Integer> tagCounts = new HashMap<>();
        Map<String, Integer> portProtocolCounts = new HashMap<>();
        int untaggedCount = 0;

        for (String[] log : logs) {
            String dstport = log[0];
            String protocol = log[1];
            String key = dstport + "," + protocol;

            // Find tag
            String tag = lookup.getOrDefault(key, "untagged");

            // Count tags
            tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);

            // Count port/protocol combinations
            portProtocolCounts.put(key, portProtocolCounts.getOrDefault(key, 0) + 1);

            if (tag.equals("untagged")) {
                untaggedCount++;
            }
        }

        Map<String, Object> results = new HashMap<>();
        results.put("tagCounts", tagCounts);
        results.put("portProtocolCounts", portProtocolCounts);
        results.put("untaggedCount", untaggedCount);
        return results;
    }

    
    public static void generateOutput(Map<String, Object> results, String outputFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {

            //Tag Counts
            writer.write("Tag Counts:\nTag,Count\n");
            Map<String, Integer> tagCounts = (Map<String, Integer>) results.get("tagCounts");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }



            //Port/Protocol Combination Counts
            writer.write("\nPort/Protocol Combination Counts:\nPort,Protocol,Count\n");

            Map<String, Integer> portProtocolCounts = (Map<String, Integer>) results.get("portProtocolCounts");

            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                String[] parts = entry.getKey().split(",");
                writer.write(parts[0] + "," + parts[1] + "," + entry.getValue() + "\n");
            }
        }
    }

    public static void main(String[] args) throws java.io.IOException {

        long startTime = System.currentTimeMillis();
        // File paths
        String lookupTablePath = "lookup_file.txt";
        String flowLogsPath = "flow_logs.txt";
        String outputFile = "output.txt";

        try {
            // Parse input files
            Map<String, String> lookup = parseLookupTable(lookupTablePath);
            List<String[]> logs = parseFlowLogs(flowLogsPath);

            // Process logs
            Map<String, Object> results = processLogs(logs, lookup);

            // Generate output
            generateOutput(results, outputFile);

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
         long responseTime = System.currentTimeMillis() - startTime;

         System.out.println("Time taken to process logs and generate mappings is  " + responseTime);
    }
}

