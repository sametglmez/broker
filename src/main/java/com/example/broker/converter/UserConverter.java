package com.example.broker.converter;

import com.example.broker.dto.*;
import com.example.broker.entity.*;
import com.example.broker.security.SensitiveData;

import java.util.stream.Collectors;

public class UserConverter {

    public static UserDto toDto(User user) {
        if (user == null) return null;

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .customer(CustomerConverter.toDto(user.getCustomer()))
                .role(RoleConverter.toDto(user.getRole()))
                .build();
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .password(dto.getPassword() != null ? dto.getPassword() : null)
                .customer(CustomerConverter.toEntity(dto.getCustomer()))
                .role(RoleConverter.toEntity(dto.getRole()))
                .build();
    }
}