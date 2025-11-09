package com.example.broker.service.strategy;


import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuyOrderStrategyTest {

    private AssetRepository assetRepository;
    private BuyOrderStrategy buyOrderStrategy;
    private Customer customer;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        buyOrderStrategy = new BuyOrderStrategy(assetRepository);

        customer = Customer.builder()
                .id(1L)
                .build();
    }

    @Test
    void createOrder_shouldBlockUsableTRY_whenBalanceSufficient() {
        OrderDto orderDto = new OrderDto();
        orderDto.setAssetName("BTC");
        orderDto.setOrderSide(null); // Strategy branch zaten BUY
        orderDto.setSize(BigDecimal.valueOf(2));
        orderDto.setPrice(BigDecimal.valueOf(100));

        Asset tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(500))
                .usableSize(BigDecimal.valueOf(500))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        buyOrderStrategy.createOrder(orderDto, customer);

        // usableSize düşmeli
        assertEquals(BigDecimal.valueOf(300), tryAsset.getUsableSize());

        // save çağrıldığını doğrula
        verify(assetRepository, times(1)).save(tryAsset);
    }

    @Test
    void createOrder_shouldThrowException_whenBalanceInsufficient() {
        OrderDto orderDto = new OrderDto();
        orderDto.setAssetName("BTC");
        orderDto.setSize(BigDecimal.valueOf(10));
        orderDto.setPrice(BigDecimal.valueOf(100));

        Asset tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(500))
                .usableSize(BigDecimal.valueOf(500))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        CustomException ex = assertThrows(CustomException.class,
                () -> buyOrderStrategy.createOrder(orderDto, customer));

        assertEquals(ErrorType.INSUFFICIENT_FUNDS, ex.getErrorType());
        verify(assetRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shouldRefundTRY() {
        Asset tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(500))
                .usableSize(BigDecimal.valueOf(300))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        OrderDto orderDto = new OrderDto();
        orderDto.setSize(BigDecimal.valueOf(200));
        orderDto.setPrice(BigDecimal.valueOf(1));

        // Mock Order nesnesi
        var order = org.mockito.Mockito.mock(com.example.broker.entity.Order.class);
        when(order.getPrice()).thenReturn(BigDecimal.valueOf(1));
        when(order.getSize()).thenReturn(BigDecimal.valueOf(200));

        buyOrderStrategy.cancelOrder(order, customer);

        assertEquals(BigDecimal.valueOf(500), tryAsset.getUsableSize());
        verify(assetRepository, times(1)).save(tryAsset);
    }

    @Test
    void matchOrder_shouldUpdateTRYAndAsset() {
        Asset tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(500))
                .usableSize(BigDecimal.valueOf(300))
                .build();

        Asset btcAsset = Asset.builder()
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(0))
                .usableSize(BigDecimal.valueOf(0))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerAndAssetName(customer, "BTC"))
                .thenReturn(Optional.of(btcAsset));

        var order = org.mockito.Mockito.mock(com.example.broker.entity.Order.class);
        when(order.getPrice()).thenReturn(BigDecimal.valueOf(100));
        when(order.getSize()).thenReturn(BigDecimal.valueOf(2));
        when(order.getAssetName()).thenReturn("BTC");

        buyOrderStrategy.matchOrder(order, customer);

        // TRY düşmeli
        assertEquals(BigDecimal.valueOf(300), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(100), tryAsset.getUsableSize()); // 300 - 200
        // BTC artmalı
        assertEquals(BigDecimal.valueOf(2), btcAsset.getSize());
        assertEquals(BigDecimal.valueOf(2), btcAsset.getUsableSize());

        verify(assetRepository, times(1)).save(tryAsset);
        verify(assetRepository, times(1)).save(btcAsset);
    }
}