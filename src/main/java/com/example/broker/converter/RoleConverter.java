package com.example.broker.converter;


import com.example.broker.dto.RoleDto;
import com.example.broker.entity.Role;

public class RoleConverter {

    public static RoleDto toDto(Role role) {
        if (role == null) return null;

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    public static Role toEntity(RoleDto dto) {
        if (dto == null) return null;

        return Role.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}