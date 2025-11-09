package com.example.broker.model;


import com.example.broker.security.SensitiveData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    @SensitiveData
    private String token;
    private String username;
    private String role;
    private Date expiresAt;
}