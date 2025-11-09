package com.example.broker.service.strategy;

import com.example.broker.dto.OrderDto;
import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import com.example.broker.entity.Order;
import com.example.broker.exception.ErrorType;
import com.example.broker.exception.CustomException;
import com.example.broker.repository.AssetRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component("SELL")
public class SellOrderStrategy implements OrderStrategy {

    private final AssetRepository assetRepository;

    public SellOrderStrategy(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void createOrder(OrderDto orderDto, Customer customer) {

        // get asset to sell
        Asset assetToSell = assetRepository.findByCustomerAndAssetName(customer, orderDto.getAssetName())
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "Asset " + orderDto.getAssetName() + " not found for customer: " + customer.getId()));

        if (assetToSell.getUsableSize().compareTo(orderDto.getSize()) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_ASSET,
                    "Asset usable balance is insufficient. required: " + orderDto.getSize() + " available: " + assetToSell.getUsableSize());
        }

        // Bloke et: usableSize -= size
        BigDecimal newUsable = assetToSell.getUsableSize().subtract(orderDto.getSize());
        assetToSell.setUsableSize(newUsable);
        assetRepository.save(assetToSell);

    }

    @Override
    public void cancelOrder(Order order, Customer customer) {

        // SELL: satılacak hisse usableSize geri verilir (size)
        Asset affectedAsset = assetRepository.findByCustomerAndAssetName(customer, order.getAssetName())
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "Asset " + order.getAssetName() + " not found for customer: " + customer.getId()));

        affectedAsset.setUsableSize(affectedAsset.getUsableSize().add(order.getSize()));
        assetRepository.save(affectedAsset);

    }

    @Override
    @Transactional
    public void matchOrder(Order order, Customer customer) {
        Asset assetToSell = assetRepository.findByCustomerAndAssetName(customer, order.getAssetName())
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "Asset " + order.getAssetName() + " not found for customer: " + customer.getId()));

        BigDecimal totalValue = order.getPrice().multiply(order.getSize());

        //Satılacak varlık miktarı kontrolü
        if (assetToSell.getSize().compareTo(order.getSize()) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_ASSET,
                    "Insufficient asset balance for matching. Required: " + order.getSize() + ", Available: " + assetToSell.getSize());
        }

        assetToSell.setSize(assetToSell.getSize().subtract(order.getSize()));
        assetToSell.setUsableSize(assetToSell.getUsableSize().subtract(order.getSize()));

        // TRY asset'i bul
        Asset tryAsset = assetRepository.findByCustomerAndAssetName(customer, "TRY")
                .orElseThrow(() -> new CustomException(ErrorType.ASSET_NOT_FOUND,
                        "TRY asset not found for customer: " + customer.getId()));

        tryAsset.setSize(tryAsset.getSize().add(totalValue));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalValue));

        assetRepository.save(assetToSell);
        assetRepository.save(tryAsset);
    }
}