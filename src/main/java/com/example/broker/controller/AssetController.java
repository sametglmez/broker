package com.example.broker.controller;

import com.example.broker.dto.AssetDto;
import com.example.broker.security.CheckRoleAccess;
import com.example.broker.service.AssetService;
import com.example.broker.service.strategy.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final Map<String, RoleValidator> roleValidator;

    @CheckRoleAccess(customerIdParam = "customerId")
    @GetMapping("/{customerId}")
    public ResponseEntity<List<AssetDto>> getAssetsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(   assetService.getAssetsByCustomerId(customerId));
    }
}