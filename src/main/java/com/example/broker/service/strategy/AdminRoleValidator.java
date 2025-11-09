package com.example.broker.service.strategy;

import com.example.broker.entity.User;
import org.springframework.stereotype.Component;

@Component("ADMIN")
public class AdminRoleValidator implements RoleValidator {

    @Override
    public boolean validateAccess(User currentUser, Long targetCustomerId) {
        // Admin her şeye erişebilir
        return true;
    }
}