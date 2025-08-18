package com.example.user_service.repository;

import java.util.Map;

public interface ICouchbaseTokenRepository {
    void saveTokens(String tokenKey, Map<String, String> tokens);
    Map<String, String> getTokens(String tokenKey);
    void removeTokens(String tokenKey);
}