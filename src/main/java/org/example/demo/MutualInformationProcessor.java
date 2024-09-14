package org.example.demo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MutualInformationProcessor {

    public Map<String, Integer> loadAllFeatureCounts(File baseFolder) throws IOException {
        Map<String, Integer> allFeatureCounts = new HashMap<>();
        for (File authorFolder : baseFolder.listFiles(File::isDirectory)) {
            for (String featureFile : new String[]{"char_bigram.txt", "char_trigram.txt", "char_fourgram.txt"}) {
                File file = new File(authorFolder, featureFile);
                if (file.exists()) {
                    Map<String, Integer> featureCount = loadFeatureFile(file.getAbsolutePath());
                    featureCount.forEach((k, v) -> allFeatureCounts.merge(k, v, Integer::sum));
                }
            }
        }
        return allFeatureCounts;
    }

    // Đọc tệp feature của tác giả và trả về một map chứa feature và số lượng xuất hiện
    private Map<String, Integer> loadFeatureFile(String filePath) throws IOException {
        Map<String, Integer> featureCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    String feature = parts[0];
                    int count = Integer.parseInt(parts[1].trim());
                    featureCount.put(feature, count);
                }
            }
        }
        return featureCount;
    }

    // Tính MI cho một feature fi và tác giả Ak
    private double calculateMI(int NfiAk, int Nfi, int NAk, int NC) {
        double p_fi_Ak = (double) NfiAk / NC;
        double p_fi = (double) Nfi / NC;
        double p_Ak = (double) NAk / NC;

        double H_fi = -p_fi * Math.log(p_fi) / Math.log(2);
        double H_Ak = -p_Ak * Math.log(p_Ak) / Math.log(2);
        double H_fi_Ak = -p_fi_Ak * Math.log(p_fi_Ak) / Math.log(2);

        return H_fi + H_Ak - H_fi_Ak;
    }

    // Ghi kết quả vào file
    private void writeTopFeaturesToFile(String folderPath, String featureType, Map<String, Double> features) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderPath, featureType)))) {
            features.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(100)
                    .forEach(entry -> {
                        try {
                            writer.write(entry.getKey() + "|" + entry.getValue());
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    // Hàm tính MI cho bigram, trigram, và fourgram
    public void processFeatures(String folderPath, String featureFile, Map<String, Integer> allFeatureCounts, int NC) throws IOException {
        Map<String, Integer> authorFeatureCounts = loadFeatureFile(folderPath + File.separator + featureFile);

        Map<String, Double> mutualInformation = new HashMap<>();
        for (String feature : authorFeatureCounts.keySet()) {
            int Nfi = allFeatureCounts.getOrDefault(feature, 0);
            int NfiAk = authorFeatureCounts.getOrDefault(feature, 0);
            int NAk = 500; // Số lượng từ của từng tác giả (500 từ)

            double MI = calculateMI(NfiAk, Nfi, NAk, NC);
            if (!Double.isNaN(MI) && !Double.isInfinite(MI)) {
                mutualInformation.put(feature, MI);
            }
        }

        writeTopFeaturesToFile(folderPath, featureFile.replace(".txt", "_top_100.txt"), mutualInformation);
    }
}
