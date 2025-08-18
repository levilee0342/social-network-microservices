package com.example.post_service.client;

import com.example.post_service.dto.crypto.AESDecryptionRequest;
import com.example.post_service.dto.crypto.AESEncryptionRequest;
import com.example.post_service.dto.crypto.RSADecryptKeyRequest;
import com.example.post_service.dto.crypto.RSAEncryptKeyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "crypto-service")
public interface CryptoFeignClient {

    @PostMapping("/crypto/aes/encrypt")
    String encryptAES(@RequestBody AESEncryptionRequest request);

    @PostMapping("/crypto/aes/decrypt")
    String decryptAES(@RequestBody AESDecryptionRequest request);

    @PostMapping("/crypto/rsa/encrypt-key")
    String encryptAESKeyRSA(@RequestBody RSAEncryptKeyRequest request);

    @PostMapping("/crypto/rsa/decrypt-key")
    String decryptAESKeyRSA(@RequestBody RSADecryptKeyRequest request);
}