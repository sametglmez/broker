package com.example.broker.service.strategy;

import com.example.broker.entity.User;
import org.springframework.stereotype.Component;

@Component("CUSTOMER")
public class CustomerRoleValidator implements RoleValidator {

    @Override
    public boolean validateAccess(User currentUser, Long targetCustomerId) {
        // Customer sadece kendi datasına erişebilir
        return currentUser.getCustomer().getId().equals(targetCustomerId);
    }
}