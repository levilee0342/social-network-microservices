package com.example.search_service.service.impl;

import com.example.search_service.service.interfaces.IUserValidator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserValidatorImpl implements IUserValidator {
    @Override
    public String validateUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof String userId)) {
            throw new IllegalStateException("User validation failed");
        }
        return userId;
    }
}
