package com.example.user_service.service.security;

public interface UserKeyService {
    String getPublicKeyBase64(String userId);
    String getPrivateKeyBase64FromToken(String token);
}
