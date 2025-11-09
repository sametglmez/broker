package com.example.broker.service;

import com.example.broker.dto.AssetDto;
import com.example.broker.entity.Asset;

import java.util.List;

public interface AssetService {
    List<AssetDto> getAssetsByCustomerId(Long customerId);
}