package com.example.broker.converter;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    public OrderDto toDto(Order order) {
        if (order == null) return null;

        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .assetName(order.getAssetName())
                .orderSide(order.getOrderSide())
                .size(order.getSize())
                .price(order.getPrice())
                .status(order.getStatus())
                .createDate(order.getCreateDate())
                .build();
    }

    public Order toEntity(OrderDto dto, Customer customer) {
        if (dto == null) return null;

        return Order.builder()
                .id(dto.getId())
                .customer(customer)
                .assetName(dto.getAssetName())
                .orderSide(dto.getOrderSide())
                .size(dto.getSize())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .createDate(dto.getCreateDate())
                .build();
    }

    public List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}