package com.example.broker.service.strategy;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;

public interface OrderStrategy {

    void createOrder(OrderDto orderDto, Customer customer);

    void cancelOrder(Order order, Customer customer);

    void matchOrder(Order order, Customer customer);
}