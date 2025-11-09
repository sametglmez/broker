package com.example.broker.service.strategy;

import com.example.broker.entity.User;

public interface RoleValidator {
    boolean validateAccess(User currentUser, Long targetCustomerId);
}