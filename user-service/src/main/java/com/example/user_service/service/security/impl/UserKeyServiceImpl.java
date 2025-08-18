package com.example.user_service.service.security.impl;

import com.example.user_service.entity.UserKey;
import com.example.user_service.repository.IUserKeyRepository;
import com.example.user_service.service.security.UserKeyService;
import com.example.user_service.utils.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Base64;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserKeyServiceImpl implements UserKeyService {

    private final IUserKeyRepository userKeyRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public String getPublicKeyBase64(String userId) {
        return getOrCreateKeyPair(userId).getPublicKeyBase64();
    }
    @Transactional
    @Override
    public String getPrivateKeyBase64FromToken(String token) {
        String userId = extractUserIdFromToken(token);
        return getOrCreateKeyPair(userId).getPrivateKeyBase64();
    }

    private UserKey getOrCreateKeyPair(String userId) {
        return userKeyRepository.findById(userId).orElseGet(() -> {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();

                String pubKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
                String privKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

                System.out.println("🔐 pubKey = " + pubKey);
                System.out.println("🔐 privKey = " + privKey);

                UserKey newKey = new UserKey();
                newKey.setUserId(userId);
                newKey.setPublicKeyBase64(pubKey);
                newKey.setPrivateKeyBase64(privKey);

                return userKeyRepository.save(newKey);
            } catch (Exception e) {
                throw new RuntimeException("Cannot generate RSA key", e);
            }
        });
    }

    private String extractUserIdFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
}
