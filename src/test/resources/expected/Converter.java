package com.example;

public interface Converter<F, T> {
    T convert(F input);

    default F identity(F input) {
        return input;
    }
}
