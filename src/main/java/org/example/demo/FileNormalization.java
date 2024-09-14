package org.example.demo;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileNormalization {

    private static final int TARGET_WORD_COUNT = 500;

    public static String normalizeFile(String inputFilePath, String outputFilePath) throws IOException {
        // Đọc nội dung file
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));

        // Sử dụng Apache OpenNLP để tách từ
        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(content);
        List<String> words = new ArrayList<>(List.of(tokens));

        // Cắt hoặc bổ sung từ để đạt đúng 500 từ
        if (words.size() > TARGET_WORD_COUNT) {
            words = words.subList(0, TARGET_WORD_COUNT);
            writeToFile(words, outputFilePath);
            return "File has been truncated to 500 words successfully.";
        } else if (words.size() < TARGET_WORD_COUNT) {
            writeToFile(words, outputFilePath);
            return "File has fewer than 500 words. Please add more words to the file.";
        } else {
            writeToFile(words, outputFilePath);
            return "File already has 500 words successfully.";
        }
    }

    private static void writeToFile(List<String> words, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (String word : words) {
                writer.write(word + " ");
            }
        }
    }
}
