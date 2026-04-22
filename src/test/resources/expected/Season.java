package com.example;

public enum Season {
    SPRING("warm"),
    SUMMER("hot"),
    AUTUMN("cool"),
    WINTER("cold");

    private final java.lang.String description;

    Season(java.lang.String description) {
        this.description = description;
    }

    public java.lang.String getDescription() {
        return description;
    }
}
