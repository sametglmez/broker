package com.example.broker.security;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveData {
}