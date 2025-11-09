package com.example.broker.service;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);

    List<OrderDto> getOrders(Long customerId);

    void cancelOrder(Long orderId);

    OrderDto matchOrder(Long orderId);
}