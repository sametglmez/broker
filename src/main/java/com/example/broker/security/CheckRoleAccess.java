package com.example.broker.security;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckRoleAccess {
    /**
     * Method parametresinin adını yazın.
     * Eğer Long tipinde ise direkt alınır.
     * Eğer DTO ise, DTO içindeki getCustomerId() çağrılır.
     */
    String customerIdParam();
}