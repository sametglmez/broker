package com.example.broker.model;

import com.example.broker.security.SensitiveData;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RegisterRequest {
    private String username;
    @SensitiveData
    private String password;
    private String role;
    private Long customerId;
}