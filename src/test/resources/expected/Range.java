package com.example;

public record Range(int min, int max) {
    Range {
        if (min > max) {
            throw new java.lang.IllegalArgumentException("min > max");
        }
    }

    public int size() {
        return max - min;
    }
}
