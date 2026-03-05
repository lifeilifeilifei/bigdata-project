package com.example.similarity.model;

public class SimilarityResponse {
    private String a;
    private String b;
    private double c;

    public SimilarityResponse(String a, String b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}
