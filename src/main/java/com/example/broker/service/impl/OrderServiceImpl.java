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
import com.example.broker.service.OrderService;
import com.example.broker.service.strategy.OrderStrategy;
import com.example.broker.service.strategy.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderConverter orderConverter;
    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final Map<String, OrderStrategy> orderStrategies;
    private final Map<String, RoleValidator> roleValidator;

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {


        Customer customer = customerRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new CustomException(ErrorType.CUSTOMER_NOT_FOUND,
                        "Customer not found with id: " + orderDto.getCustomerId()));

        // 2) Kurallar: BUY => TRY usableSize >= price * size
        //             SELL => ilgili asset usableSize >= size
        OrderSide side = orderDto.getOrderSide();
        BigDecimal size = orderDto.getSize();
        BigDecimal price = orderDto.getPrice();

        if (side == null || size == null || price == null) {
            throw new CustomException(ErrorType.INVALID_REQUEST, "Order side/size/price must be provided");
        }

        if (size.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorType.INVALID_REQUEST, "Size and price must be positive");
        }

        OrderStrategy strategy = orderStrategies.get(orderDto.getOrderSide().name());
        strategy.createOrder(orderDto, customer);

        orderDto.setStatus(OrderStatus.PENDING);
        orderDto.setCreateDate(LocalDateTime.now());
        Order orderEntity = orderConverter.toEntity(orderDto, customer);
        Order saved = orderRepository.save(orderEntity);
        return orderConverter.toDto(saved);
    }

    @Override
    @Transactional
    public List<OrderDto> getOrders(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ErrorType.CUSTOMER_NOT_FOUND));

        var orders = orderRepository.findByCustomer(customer);
        return orderConverter.toDtoList(orders);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        // TODO : Customer_id yi de al
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorType.ORDER_NOT_FOUND,
                        "Order not found with id: " + orderId));

        // 2) Yalnızca PENDING iptal edilebilir
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ErrorType.ORDER_NOT_PENDING,
                    "Order is not in PENDING status: current=" + order.getStatus());
        }

        // 3) İlgili müşteri ve asset bulunur
        Customer customer = order.getCustomer();
        if (customer == null) {
            throw new CustomException(ErrorType.CUSTOMER_NOT_FOUND,
                    "Customer not found for order: " + orderId);
        }

        OrderStrategy strategy = orderStrategies.get(order.getOrderSide().name());
        strategy.cancelOrder(order, customer);

        // 5) Order status -> CANCELED
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

    }

    @Override
    @Transactional
    public OrderDto matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorType.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ErrorType.ORDER_NOT_PENDING,
                    "Only pending orders can be matched");
        }

        Customer customer = order.getCustomer();
        OrderStrategy strategy = orderStrategies.get(order.getOrderSide().name());
        strategy.matchOrder(order, customer);

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);

        return orderConverter.toDto(order);
    }
}