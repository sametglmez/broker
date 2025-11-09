package com.example.broker.service.impl;


import com.example.broker.converter.AssetConverter;
import com.example.broker.dto.AssetDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.repository.AssetRepository;
import com.example.broker.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceImplTest {

    private AssetRepository assetRepository;
    private CustomerRepository customerRepository;
    private AssetConverter assetConverter;
    private AssetServiceImpl assetService;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        customerRepository = mock(CustomerRepository.class);
        assetConverter = mock(AssetConverter.class);
        assetService = new AssetServiceImpl(assetRepository, assetConverter, customerRepository);
    }

    @Test
    void getAssetsByCustomerId_shouldReturnAssetDtos_whenCustomerExists() {
        Customer customer = Customer.builder().id(1L).build();
        Asset asset = Asset.builder()
                .id(10L)
                .customer(customer)
                .assetName("BTC")
                .size(BigDecimal.valueOf(5))
                .usableSize(BigDecimal.valueOf(5))
                .build();

        List<Asset> assets = List.of(asset);
        List<AssetDto> assetDtos = List.of(new AssetDto()); // dummy DTO

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByCustomer(customer)).thenReturn(assets);
        when(assetConverter.toDtoList(assets)).thenReturn(assetDtos);

        List<AssetDto> result = assetService.getAssetsByCustomerId(1L);

        assertEquals(assetDtos, result);
        verify(customerRepository, times(1)).findById(1L);
        verify(assetRepository, times(1)).findByCustomer(customer);
        verify(assetConverter, times(1)).toDtoList(assets);
    }

    @Test
    void getAssetsByCustomerId_shouldThrowException_whenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> assetService.getAssetsByCustomerId(1L));

        assertEquals(ErrorType.CUSTOMER_NOT_FOUND, ex.getErrorType());
        verify(assetRepository, never()).findByCustomer(any());
        verify(assetConverter, never()).toDtoList(any());
    }
}