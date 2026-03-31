package com.example.student.university_career_platform.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CosineSimilarityUtil {

    public static double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.trim().isEmpty() || text2.trim().isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> vector1 = getTermFrequency(text1);
        Map<String, Integer> vector2 = getTermFrequency(text2);

        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(vector1.keySet());
        allTerms.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String term : allTerms) {
            int val1 = vector1.getOrDefault(term, 0);
            int val2 = vector2.getOrDefault(term, 0);

            dotProduct += val1 * val2;
            magnitude1 += val1 * val1;
            magnitude2 += val2 * val2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }

    private static Map<String, Integer> getTermFrequency(String text) {
        Map<String, Integer> map = new HashMap<>();
        text = text.replaceAll("[\\pP\\p{Punct}]", "").toLowerCase();

        for (int i = 0; i < text.length() - 1; i++) {
            String term = text.substring(i, i + 2);
            map.put(term, map.getOrDefault(term, 0) + 1);
        }
        if (text.length() == 1) {
            map.put(text, 1);
        }
        return map;
    }
}