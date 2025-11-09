package com.example.broker.repository;

import com.example.broker.entity.Asset;
import com.example.broker.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCustomer(Customer customer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Asset> findByCustomerAndAssetName(Customer customer, String assetName);
}