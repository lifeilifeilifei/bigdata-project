package com.example.similarity.service;

import org.springframework.stereotype.Service;
import com.example.similarity.model.SimilarityResponse;

@Service
public class SimilarityService {

    public SimilarityResponse calculateSimilarity(String a, String b) {
        double similarity = yuxianxiangsidu(a, b);

        return new SimilarityResponse(a, b, similarity);
    }

    private double yuxianxiangsidu(String a, String b) {
        // Placeholder for actual similarity calculation logic
        // Replace with your implementation
        return 0.5; // Example similarity score
    }
}
