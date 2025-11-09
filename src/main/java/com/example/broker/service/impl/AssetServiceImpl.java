package com.example.broker.service.impl;

import com.example.broker.converter.AssetConverter;
import com.example.broker.dto.AssetDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.exception.CustomException;
import com.example.broker.exception.ErrorType;
import com.example.broker.repository.AssetRepository;
import com.example.broker.repository.CustomerRepository;
import com.example.broker.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetConverter assetConverter;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public List<AssetDto> getAssetsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ErrorType.CUSTOMER_NOT_FOUND));

        return assetConverter.toDtoList(assetRepository.findByCustomer(customer));
    }
}