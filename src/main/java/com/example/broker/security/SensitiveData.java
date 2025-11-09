package com.example.broker.security;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonValue;

public class SensitiveData<T> {
    private final T value;

    public SensitiveData(T value) {
        this.value = value;
    }

    public T getValue() {
        return value; // normal kullanım için orijinal veri
    }

    @JsonValue // JSON’a yazarken bu metodu kullanır
    public String maskedValue() {
        if (value == null) return null;
        String str = value.toString();
        if (str.length() <= 2) return "***";
        return str.charAt(0) + "***" + str.charAt(str.length() - 1);
    }

    @Override
    public String toString() {
        return maskedValue(); // loglarda da maskelenmiş görünsün
    }
}