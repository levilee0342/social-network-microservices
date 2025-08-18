package com.example.user_service.repository;

import com.example.user_service.entity.User;

public interface ICouchbaseUserRepository {
    boolean exists(String userKey);
    void saveUser(String userKey, User user);
    User getUser(String userKey);
}
