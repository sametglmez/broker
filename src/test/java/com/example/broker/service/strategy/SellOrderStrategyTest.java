package com.example.broker.service.strategy;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SellOrderStrategyTest {

    private AssetRepository assetRepository;
    private SellOrderStrategy sellOrderStrategy;
    private Customer customer;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        sellOrderStrategy = new SellOrderStrategy(assetRepository);

        customer = Customer.builder()
                .id(1L)
                .build();
    }

    @Test
    void createOrder_shouldBlockAsset_whenUsableSufficient() {
        OrderDto orderDto = new OrderDto();
        orderDto.setAssetName("BTC");
        orderDto.setSize(BigDecimal.valueOf(2));
        orderDto.setPrice(BigDecimal.valueOf(100));

        Asset assetToSell = Asset.builder()
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(5))
                .usableSize(BigDecimal.valueOf(5))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "BTC"))
                .thenReturn(Optional.of(assetToSell));

        sellOrderStrategy.createOrder(orderDto, customer);

        assertEquals(BigDecimal.valueOf(3), assetToSell.getUsableSize());
        verify(assetRepository, times(1)).save(assetToSell);
    }

    @Test
    void createOrder_shouldThrowException_whenUsableInsufficient() {
        OrderDto orderDto = new OrderDto();
        orderDto.setAssetName("BTC");
        orderDto.setSize(BigDecimal.valueOf(10));
        orderDto.setPrice(BigDecimal.valueOf(100));

        Asset assetToSell = Asset.builder()
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(5))
                .usableSize(BigDecimal.valueOf(5))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "BTC"))
                .thenReturn(Optional.of(assetToSell));

        CustomException ex = assertThrows(CustomException.class,
                () -> sellOrderStrategy.createOrder(orderDto, customer));

        assertEquals(ErrorType.INSUFFICIENT_ASSET, ex.getErrorType());
        verify(assetRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shouldRefundUsableAsset() {
        Asset assetToSell = Asset.builder()
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(5))
                .usableSize(BigDecimal.valueOf(3))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "BTC"))
                .thenReturn(Optional.of(assetToSell));

        var order = mock(com.example.broker.entity.Order.class);
        when(order.getSize()).thenReturn(BigDecimal.valueOf(2));
        when(order.getAssetName()).thenReturn("BTC");

        sellOrderStrategy.cancelOrder(order, customer);

        assertEquals(BigDecimal.valueOf(5), assetToSell.getUsableSize());
        verify(assetRepository, times(1)).save(assetToSell);
    }

    @Test
    void matchOrder_shouldUpdateAssetAndTRY() {
        Asset assetToSell = Asset.builder()
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(5))
                .usableSize(BigDecimal.valueOf(3))
                .build();

        Asset tryAsset = Asset.builder()
                .customer(customer)
                .assetName("TRY")
                .size(BigDecimal.valueOf(500))
                .usableSize(BigDecimal.valueOf(500))
                .build();

        when(assetRepository.findByCustomerAndAssetName(customer, "BTC"))
                .thenReturn(Optional.of(assetToSell));
        when(assetRepository.findByCustomerAndAssetName(customer, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        var order = mock(com.example.broker.entity.Order.class);
        when(order.getSize()).thenReturn(BigDecimal.valueOf(2));
        when(order.getPrice()).thenReturn(BigDecimal.valueOf(100));
        when(order.getAssetName()).thenReturn("BTC");

        sellOrderStrategy.matchOrder(order, customer);

        // BTC düşmeli
        assertEquals(BigDecimal.valueOf(3), assetToSell.getSize());
        assertEquals(BigDecimal.valueOf(1), assetToSell.getUsableSize()); // 3 - 2
        // TRY artmalı
        assertEquals(BigDecimal.valueOf(700), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(700), tryAsset.getUsableSize());

        verify(assetRepository, times(1)).save(assetToSell);
        verify(assetRepository, times(1)).save(tryAsset);
    }
}