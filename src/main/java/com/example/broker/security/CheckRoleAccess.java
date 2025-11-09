package com.example.broker.security;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckRoleAccess {
    String customerIdParam();
}