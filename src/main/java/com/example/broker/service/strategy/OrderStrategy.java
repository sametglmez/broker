package com.example.broker.service.strategy;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;

public interface OrderStrategy {

    // Yeni emir oluşturma davranışı
    void createOrder(OrderDto orderDto, Customer customer);

    // Mevcut emri iptal etme davranışı
    void cancelOrder(Order order, Customer customer);

    void matchOrder(Order order, Customer customer);
}