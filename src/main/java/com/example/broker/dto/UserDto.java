package com.example.broker.dto;

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
    private String password; // hassas veri
    private RoleDto role;
}