package com.example.broker.converter;

import com.example.broker.dto.AssetDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssetConverter {

    public AssetDto toDto(Asset asset) {
        if (asset == null) return null;

        return AssetDto.builder()
                .id(asset.getId())
                .customerId(asset.getCustomer().getId())
                .assetName(asset.getAssetName())
                .size(asset.getSize())
                .usableSize(asset.getUsableSize())
                .build();
    }

    public Asset toEntity(AssetDto dto, Customer customer) {
        if (dto == null) return null;

        return Asset.builder()
                .id(dto.getId())
                .customer(customer)
                .assetName(dto.getAssetName())
                .size(dto.getSize())
                .usableSize(dto.getUsableSize())
                .build();
    }

    public List<AssetDto> toDtoList(List<Asset> assets) {
        return assets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}