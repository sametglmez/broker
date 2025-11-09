package com.example.broker.dto;

import com.example.broker.security.SensitiveData;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private CustomerDto customer;
    private String username;
    @SensitiveData
    private String password; // hassas veri
    private RoleDto role;
}