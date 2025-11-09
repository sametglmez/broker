package com.example.broker.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;        // JWT token
    private String username;     // Opsiyonel, istersen ekleyebilirsin
    private String role;         // Kullanıcının rolü
    private Date expiresAt;      // Token expiration timestamp (ms)
}