package com.example.broker.service.strategy;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;
import com.example.broker.enums.OrderStatus;
import com.example.broker.exception.ErrorType;
import com.example.broker.exception.CustomException;
import com.example.broker.repository.AssetRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component("BUY")
public class BuyOrderStrategy implements OrderStrategy {

    private final AssetRepository assetRepository;

    public BuyOrderStrategy(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    @Transactional
    public void createOrder(OrderDto orderDto, Customer customer) {
        // get TRY asset for customer
        Asset tryAsset = assetRepository.findByCustomerAndAssetName(customer, "TRY")
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "TRY asset not found for customer: " + customer.getId()));

        BigDecimal totalCost = orderDto.getPrice().multiply(orderDto.getSize()); // price * size
        // Kullanılabilir bakiye kontrolü
        if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_FUNDS,
                    "TRY usable balance is insufficient. required: " + totalCost + " available: " + tryAsset.getUsableSize());
        }

        // Bloke et: usableSize -= totalCost
        BigDecimal newTryUsable = tryAsset.getUsableSize().subtract(totalCost);
        tryAsset.setUsableSize(newTryUsable);
        assetRepository.save(tryAsset);

    }

    @Override
    @Transactional
    public void cancelOrder(Order order, Customer customer) {
        // BUY: TRY usableSize geri verilir (price * size)
        Asset affectedAsset = assetRepository.findByCustomerAndAssetName(customer, "TRY")
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "TRY asset not found for customer: " + customer.getId()));

        BigDecimal refund = order.getPrice().multiply(order.getSize());
        affectedAsset.setUsableSize(affectedAsset.getUsableSize().add(refund));
        assetRepository.save(affectedAsset);

    }

    @Override
    @Transactional
    public void matchOrder(Order order, Customer customer) {
        Asset tryAsset = assetRepository.findByCustomerAndAssetName(customer, "TRY")
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND, "TRY asset not found for customer: " + customer.getId()));

        BigDecimal totalCost = order.getPrice().multiply(order.getSize());

        // Bakiye yeterliliği kontrolü
        if (tryAsset.getSize().compareTo(totalCost) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_FUNDS,
                    "Insufficient TRY balance for matching. Required: " + totalCost + ", Available: " + tryAsset.getSize());
        }

        // TRY düşülür
        tryAsset.setSize(tryAsset.getSize().subtract(totalCost));
        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));

        // Alınan varlık (örneğin BTC) bulunur veya oluşturulur
        Asset boughtAsset = assetRepository.findByCustomerAndAssetName(customer, order.getAssetName())
                .orElseGet(() -> Asset.builder()
                        .customer(customer)
                        .assetName(order.getAssetName())
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .build());

        boughtAsset.setSize(boughtAsset.getSize().add(order.getSize()));
        boughtAsset.setUsableSize(boughtAsset.getUsableSize().add(order.getSize()));

        assetRepository.save(tryAsset);
        assetRepository.save(boughtAsset);
    }
}