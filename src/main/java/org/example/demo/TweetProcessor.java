package org.example.demo;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import com.atlascopco.hunspell.Hunspell;

import java.io.*;
import java.util.*;

public class TweetProcessor {
    private static String tweetContent;
    private static final Map<Character, String> punctuationToWords = new HashMap<>();

    static {
        punctuationToWords.put('.', "dot");
        punctuationToWords.put(',', "comma");
        punctuationToWords.put('!', "exclamation");
        punctuationToWords.put('?', "question");
        punctuationToWords.put(';', "semicolon");
        punctuationToWords.put(':', "colon");
        punctuationToWords.put('\'', "apostrophe");
        punctuationToWords.put('"', "quotation");
        punctuationToWords.put('-', "hyphen");
        punctuationToWords.put('(', "left_paren");
        punctuationToWords.put(')', "right_paren");
        punctuationToWords.put('[', "left_bracket");
        punctuationToWords.put(']', "right_bracket");
        punctuationToWords.put('{', "left_brace");
        punctuationToWords.put('}', "right_brace");
    }

    private static Set<String> functionWords = new HashSet<>();

    public static void setTweetContent(String content) {
        tweetContent = content;
    }

    public static String analyzeTweet(String filePath) throws Exception {
        StringBuilder result = new StringBuilder();

        try {

            String folderPath = filePath.replace(".txt", "_analysis");
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();  // Tạo thư mục nếu chưa tồn tại
            }

            // Load models
            InputStream modelIn = new FileInputStream("src/main/resources/en-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(modelIn);
            TokenizerME tokenizer = new TokenizerME(tokenModel);

            InputStream posModelIn = new FileInputStream("src/main/resources/en-pos-maxent.bin");
            POSModel posModel = new POSModel(posModelIn);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            String analysisFilePath = folderPath + "/summary.txt";
            String bigramFilePath = folderPath + "/bigram.txt";
            String trigramFilePath = folderPath + "/trigram.txt";
            String wordPercentageFilePath = folderPath + "/word_char_percentage.txt";
            String charPercentageFilePath = folderPath + "/char_percentage.txt";
            String bagOfWordsFilePath = folderPath + "/bag_of_words.txt";
            String charBigramFilePath = folderPath + "/char_bigram.txt";
            String charTrigramFilePath = folderPath + "/char_trigram.txt";
            String charFourgramFilePath = folderPath + "/char_fourgram.txt";
            loadFunctionWords("src/main/resources/function_words.txt");

            // Use TokenizerME to tokenize the tweet
            String[] words = tokenizer.tokenize(tweetContent);
            int totalWords = words.length;

            // Total number of characters
            int totalCharacters = tweetContent.length();

            // POS Tagging
            String[] posTags = posTagger.tag(words);

            // Count function words
            Map<String, Integer> functionWordCount = new HashMap<>();

            // bigram and trigram
            Map<String, Integer> bigramCount = new HashMap<>();
            Map<String, String> bigramPosTags = new HashMap<>();

            Map<String, Integer> trigramCount = new HashMap<>();
            Map<String, String> trigramPosTags = new HashMap<>();

            // Tính toán bigram, trigram và fourgram ký tự
            Map<String, Integer> charBigramCount = new HashMap<>();
            Map<String, Integer> charTrigramCount = new HashMap<>();
            Map<String, Integer> charFourgramCount = new HashMap<>();

            for (String word : words) {
                String lowerCaseWord = word.toLowerCase();
                if (functionWords.contains(lowerCaseWord)) {
                    functionWordCount.put(lowerCaseWord, functionWordCount.getOrDefault(lowerCaseWord, 0) + 1);
                }
            }

            // Punctuation (Dấu câu)
            String punctuations = ".,!?;:'\"-()[]{}";
            Map<Character, Integer> punctuationCount = new HashMap<>();
            for (char c : tweetContent.toCharArray()) {
                if (punctuations.indexOf(c) >= 0) {
                    punctuationCount.put(c, punctuationCount.getOrDefault(c, 0) + 1);
                }
            }

            // POS Tag Bigrams and Trigrams
            Map<String, Integer> posBigramCount = new HashMap<>();
            Map<String, Integer> posTrigramCount = new HashMap<>();
            for (int i = 0; i < posTags.length - 1; i++) {
                String bigram = posTags[i] + " " + posTags[i + 1];
                posBigramCount.put(bigram, posBigramCount.getOrDefault(bigram, 0) + 1);
                if (i < posTags.length - 2) {
                    String trigram = posTags[i] + " " + posTags[i + 1] + " " + posTags[i + 2];
                    posTrigramCount.put(trigram, posTrigramCount.getOrDefault(trigram, 0) + 1);
                }
            }

            // Character percentage per word
            Map<String, Double> wordCharPercentage = new HashMap<>();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                double percentage = (word.length() / (double) totalCharacters) * 100;
                String posTag = posTags[i];
                wordCharPercentage.put(word + "|" + posTag, percentage);
            }

            // Character percentage in the entire tweet
            Map<Character, Double> charPercentage = new HashMap<>();
            for (char c : tweetContent.toCharArray()) {
                charPercentage.put(c, charPercentage.getOrDefault(c, 0.0) + (1.0 / totalCharacters) * 100);
            }

            // Digits, Digit Bigrams and Digit Trigrams
            int digitCount = 0;
            Map<String, Integer> digitBigrams = new HashMap<>();
            Map<String, Integer> digitTrigrams = new HashMap<>();
            for (int i = 0; i < tweetContent.length(); i++) {
                if (Character.isDigit(tweetContent.charAt(i))) {
                    digitCount++;
                    if (i < tweetContent.length() - 1 && Character.isDigit(tweetContent.charAt(i + 1))) {
                        String bigram = tweetContent.substring(i, i + 2);
                        digitBigrams.put(bigram, digitBigrams.getOrDefault(bigram, 0) + 1);
                        if (i < tweetContent.length() - 2 && Character.isDigit(tweetContent.charAt(i + 2))) {
                            String trigram = tweetContent.substring(i, i + 3);
                            digitTrigrams.put(trigram, digitTrigrams.getOrDefault(trigram, 0) + 1);
                        }
                    }
                }
            }

            // Word Length Distribution
            Map<Integer, Integer> wordLengthDistribution = new HashMap<>();
            for (String word : words) {
                int length = word.length();
                wordLengthDistribution.put(length, wordLengthDistribution.getOrDefault(length, 0) + 1);
            }

            // Lexical Richness (hapax legomena)
            Map<String, Integer> wordFrequencies = new HashMap<>();
            for (String word : words) {
                wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
            }
            long hapaxLegomena = wordFrequencies.values().stream().filter(count -> count == 1).count();

            // Special Characters
            String specialCharacters = "!@#$%^&*()_+-=<>?,./;:'\"[]{}\\|`~";
            Map<Character, Integer> specialCharCount = new HashMap<>();
            for (char c : tweetContent.toCharArray()) {
                if (specialCharacters.indexOf(c) >= 0) {
                    specialCharCount.put(c, specialCharCount.getOrDefault(c, 0) + 1);
                }
            }

            // Bag of Words
            Map<String, Integer> wordCount = new HashMap<>();
            for (String word : words) {
                wordCount.put(word.toLowerCase(), wordCount.getOrDefault(word.toLowerCase(), 0) + 1);
            }

            // Tính toán bigram từ
            for (int i = 0; i < words.length - 1; i++) {
                String bigram = replacePunctuation(words[i].toLowerCase()) + " " + replacePunctuation(words[i + 1].toLowerCase());
                String bigramPos = posTags[i] + "-" + posTags[i + 1];
                bigramCount.put(bigram, bigramCount.getOrDefault(bigram, 0) + 1);
                bigramPosTags.put(bigram, bigramPos);
            }

            // Tính toán trigram từ
            for (int i = 0; i < words.length - 2; i++) {
                String trigram = replacePunctuation(words[i].toLowerCase()) + " " + replacePunctuation(words[i + 1].toLowerCase()) + " " + replacePunctuation(words[i + 2].toLowerCase());
                String trigramPos = posTags[i] + "-" + posTags[i + 1] + "-" + posTags[i + 2];
                trigramCount.put(trigram, trigramCount.getOrDefault(trigram, 0) + 1);
                trigramPosTags.put(trigram, trigramPos);
            }

            for (int i = 0; i < tweetContent.length() - 1; i++) {
                String bigram = tweetContent.substring(i, i + 2);
                charBigramCount.put(bigram, charBigramCount.getOrDefault(bigram, 0) + 1);
                if (i < tweetContent.length() - 2) {
                    String trigram = tweetContent.substring(i, i + 3);
                    charTrigramCount.put(trigram, charTrigramCount.getOrDefault(trigram, 0) + 1);
                }
                if (i < tweetContent.length() - 3) {
                    String fourgram = tweetContent.substring(i, i + 4);
                    charFourgramCount.put(fourgram, charFourgramCount.getOrDefault(fourgram, 0) + 1);
                }
            }

            // Misspelled Words
            String[] misspelledWords = findMisspelledWords(words);

            // results bigram export to .txt file
            exportResultToFile(bigramFilePath, bigramCount, bigramPosTags);

            // Write results trigram to .txt file
            exportResultToFile(trigramFilePath, trigramCount, trigramPosTags);

            exportCharPercentagePerWordToFile(wordPercentageFilePath, wordCharPercentage);

            exportCharPercentageToFile(charPercentageFilePath, charPercentage);

            exportBagOfWordsToFile(bagOfWordsFilePath, wordCount);

            exportCharNgramToFile(charBigramFilePath, charBigramCount);

            exportCharNgramToFile(charTrigramFilePath, charTrigramCount);

            exportCharNgramToFile(charFourgramFilePath, charFourgramCount);

            // Write results to .txt file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(analysisFilePath))) {
                writer.write("Tổng số từ: " + totalWords + "\n");
                writer.write("Tổng số ký tự: " + totalCharacters + "\n");


                writer.write("\nSố chữ số trong tweet: " + digitCount + "\n");

                writer.write("\nBigrams chữ số:\n");
                for (Map.Entry<String, Integer> entry : digitBigrams.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                writer.write("\nTrigrams chữ số:\n");
                for (Map.Entry<String, Integer> entry : digitTrigrams.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                writer.write("\nPhân phối độ dài từ:\n");
                for (Map.Entry<Integer, Integer> entry : wordLengthDistribution.entrySet()) {
                    writer.write(entry.getKey() + " ký tự: " + entry.getValue() + " từ\n");
                }

                writer.write("\nĐộ phong phú từ vựng (Hapax Legomena): " + hapaxLegomena + "\n");

                writer.write("\nKý tự đặc biệt:\n");
                for (Map.Entry<Character, Integer> entry : specialCharCount.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                // Function Words
                writer.write("\nTần suất của từ chức năng:\n");
                for (Map.Entry<String, Integer> entry : functionWordCount.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                // Punctuation
                writer.write("\nTần suất của dấu câu:\n");
                for (Map.Entry<Character, Integer> entry : punctuationCount.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                // Sai chinh ta
                writer.write("\nTừ Sai Chính tả:\n");
                for (String misspelledWord : misspelledWords) {
                    writer.write(misspelledWord + "\n");
                }
            }

            return folderPath;

        } catch (Exception e) {
            throw new Exception("Đã xảy ra lỗi trong quá trình phân tích tweet: " + e.getMessage());
        }
    }

    private static void exportResultToFile(String FilePath, Map<String, Integer> Count, Map<String, String> PosTags) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FilePath))) {
            for (Map.Entry<String, Integer> entry : Count.entrySet()) {
                String trigram = entry.getKey();
                int count = entry.getValue();
                String pos = PosTags.get(trigram);
                writer.write(trigram + "|" + pos + "|" + count + "\n");
            }
        }
    }

    private static void exportCharPercentagePerWordToFile(String filePath, Map<String, Double> wordCharPercentage) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Phần trăm ký tự trên mỗi từ:\n");
            for (Map.Entry<String, Double> entry : wordCharPercentage.entrySet()) {
                writer.write(entry.getKey() + "|" + String.format("%.2f", entry.getValue()) + "%\n");
            }
        }
    }

    private static void exportCharPercentageToFile(String filePath, Map<Character, Double> charPercentage) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Phần trăm của mỗi ký tự:\n");
            for (Map.Entry<Character, Double> entry : charPercentage.entrySet()) {
                writer.write(entry.getKey() + "|" + String.format("%.2f", entry.getValue()) + "%\n");
            }
        }
    }

    private static void exportBagOfWordsToFile(String filePath, Map<String, Integer> wordCount) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Túi từ\n");
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue() + "\n");
            }
        }
    }

    private static void exportCharNgramToFile(String filePath, Map<String, Integer> ngramCount) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Integer> entry : ngramCount.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue() + "\n");
            }
        }
    }


    private static void loadFunctionWords(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            functionWords.add(line.trim().toLowerCase());
        }
        reader.close();
    }

    private static String[] findMisspelledWords(String[] words) throws IOException {

        String affFile = "src/main/resources/en_US.aff";
        String dicFile = "src/main/resources/en_US.dic";

        // create Hunspell
        Hunspell hunspell = new Hunspell(dicFile,affFile);


        // Tạo tập hợp để lưu các từ sai chính tả
        Set<String> misspelledWords = new HashSet<>();

        // Kiểm tra từng từ trong mảng
        for (String word : words) {
            // Kiểm tra từ trong một văn bản mẫu
            boolean result = hunspell.spell(word);

            // Nếu có lỗi, thêm từ vào tập hợp
            if (!result) {
                misspelledWords.add(word);
            }
        }

        // Chuyển tập hợp thành mảng
        return misspelledWords.toArray(new String[0]);
    }

    private static String replacePunctuation(String text) {
        StringBuilder replacedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (punctuationToWords.containsKey(c)) {
                replacedText.append(punctuationToWords.get(c));
            } else {
                replacedText.append(c);
            }
        }
        return replacedText.toString();
    }
}
