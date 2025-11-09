package com.example.broker.service.impl;


import com.example.broker.converter.OrderConverter;
import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;
import com.example.broker.enums.OrderSide;
import com.example.broker.enums.OrderStatus;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.repository.AssetRepository;
import com.example.broker.repository.CustomerRepository;
import com.example.broker.repository.OrderRepository;
import com.example.broker.service.strategy.OrderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private OrderConverter orderConverter;
    private CustomerRepository customerRepository;
    private AssetRepository assetRepository;
    private Map<String, OrderStrategy> orderStrategies;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderConverter = mock(OrderConverter.class);
        customerRepository = mock(CustomerRepository.class);
        assetRepository = mock(AssetRepository.class);

        // Strategy map
        OrderStrategy buyStrategy = mock(OrderStrategy.class);
        OrderStrategy sellStrategy = mock(OrderStrategy.class);
        orderStrategies = Map.of(
                "BUY", buyStrategy,
                "SELL", sellStrategy
        );

        orderService = new OrderServiceImpl(orderRepository, orderConverter,
                customerRepository, assetRepository, orderStrategies, Map.of());
    }

    @Test
    void createOrder_shouldCreateBuyOrderSuccessfully() {
        Customer customer = Customer.builder().id(1L).build();
        OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId(1L);
        orderDto.setOrderSide(OrderSide.BUY);
        orderDto.setSize(BigDecimal.valueOf(2));
        orderDto.setPrice(BigDecimal.valueOf(10));

        Order orderEntity = new Order();
        Order savedOrder = new Order();
        savedOrder.setId(100L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderConverter.toEntity(orderDto, customer)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(savedOrder);
        when(orderConverter.toDto(savedOrder)).thenReturn(orderDto);

        OrderDto result = orderService.createOrder(orderDto);

        assertEquals(orderDto, result);
        verify(orderStrategies.get("BUY"), times(1)).createOrder(orderDto, customer);
        verify(orderRepository, times(1)).save(orderEntity);
    }

    @Test
    void createOrder_shouldThrowException_whenCustomerNotFound() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId(1L);
        orderDto.setOrderSide(OrderSide.BUY);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.createOrder(orderDto));

        assertEquals(ErrorType.CUSTOMER_NOT_FOUND, ex.getErrorType());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrders_shouldReturnOrderList() {
        Customer customer = Customer.builder().id(1L).build();
        Order order = new Order();
        List<Order> orders = List.of(order);
        OrderDto dto = new OrderDto();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(orders);
        when(orderConverter.toDtoList(orders)).thenReturn(List.of(dto));

        List<OrderDto> result = orderService.getOrders(1L);

        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByCustomer(customer);
    }

    @Test
    void cancelOrder_shouldCancelPendingOrder() {
        Customer customer = Customer.builder().id(1L).build();
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).customer(customer).orderSide(OrderSide.BUY).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(orderStrategies.get("BUY"), times(1)).cancelOrder(order, customer);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_shouldThrowException_whenOrderNotPending() {
        Order order = Order.builder().id(1L).status(OrderStatus.MATCHED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.cancelOrder(1L));

        assertEquals(ErrorType.ORDER_NOT_PENDING, ex.getErrorType());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void matchOrder_shouldMatchPendingOrder() {
        Customer customer = Customer.builder().id(1L).build();
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).customer(customer).orderSide(OrderSide.SELL).build();
        OrderDto dto = new OrderDto();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderConverter.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.matchOrder(1L);

        assertEquals(dto, result);
        assertEquals(OrderStatus.MATCHED, order.getStatus());
        verify(orderStrategies.get("SELL"), times(1)).matchOrder(order, customer);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void matchOrder_shouldThrowException_whenOrderNotPending() {
        Order order = Order.builder().status(OrderStatus.CANCELED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.matchOrder(1L));

        assertEquals(ErrorType.ORDER_NOT_PENDING, ex.getErrorType());
        verify(orderRepository, never()).save(any());
    }
}