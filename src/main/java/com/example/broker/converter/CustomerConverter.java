package com.example.broker.converter;

import com.example.broker.dto.CustomerDto;
import com.example.broker.entity.Customer;

import java.util.stream.Collectors;

public class CustomerConverter {

    public static CustomerDto toDto(Customer customer) {
        if (customer == null) return null;

        return CustomerDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .assetIds(customer.getAssets() != null
                        ? customer.getAssets().stream().map(a -> a.getId()).collect(Collectors.toList())
                        : null)
                .orderIds(customer.getOrders() != null
                        ? customer.getOrders().stream().map(o -> o.getId()).collect(Collectors.toList())
                        : null)
                .build();
    }

    public static Customer toEntity(CustomerDto dto) {
        if (dto == null) return null;

        return Customer.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}