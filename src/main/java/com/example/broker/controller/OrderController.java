package com.example.broker.controller;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Order;
import com.example.broker.entity.User;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.security.CheckRoleAccess;
import com.example.broker.security.SecurityUser;
import com.example.broker.service.OrderService;
import com.example.broker.service.strategy.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final Map<String, RoleValidator> roleValidator;

    @PostMapping
    @CheckRoleAccess(customerIdParam = "customerId")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }


    @GetMapping("/{customerId}")
    @CheckRoleAccess(customerIdParam = "customerId")
    public ResponseEntity<List<OrderDto>> getOrders(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(orderService.getOrders(customerId));
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match-order/{orderId}")
    public ResponseEntity<OrderDto> matchOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.matchOrder(orderId));
    }
}