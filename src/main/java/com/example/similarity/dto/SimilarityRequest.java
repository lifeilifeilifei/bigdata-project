package com.example.similarity.dto;
// 放在你的项目包下，比如 com.example.similarity.dto
public class SimilarityRequest {
    // 字段名要和 JSON 里的 a、b 完全一致
    private String a;
    private String b;

    // 必须生成 getter/setter（否则 Spring 无法赋值）
    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }
}