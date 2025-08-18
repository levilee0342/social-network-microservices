package com.example.crypto_service.controller;

import com.example.crypto_service.dto.request.AESDecryptionRequest;
import com.example.crypto_service.dto.request.AESEncryptionRequest;
import com.example.crypto_service.dto.request.RSADecryptKeyRequest;
import com.example.crypto_service.dto.request.RSAEncryptKeyRequest;
import com.example.crypto_service.service.CryptoService;
import com.example.crypto_service.service.impl.CryptoServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crypto")
@RequiredArgsConstructor
public class CryptoController {

    private final CryptoService cryptoService;

    @PostMapping("/aes/encrypt")
    public ResponseEntity<String> encryptAES(@RequestBody AESEncryptionRequest request) {
        String ciphertext = cryptoService.encryptAES(request.getPlainText(), request.getKey());
        return ResponseEntity.ok(ciphertext);
    }

    @PostMapping("/aes/decrypt")
    public ResponseEntity<String> decryptAES(@RequestBody AESDecryptionRequest request) {
        String plaintext = cryptoService.decryptAES(request.getCipherText(), request.getKey());
        return ResponseEntity.ok(plaintext);
    }


    @PostMapping("/rsa/encrypt-key")
    public ResponseEntity<String> encryptAESKeyRSA(@RequestBody RSAEncryptKeyRequest request) {
        String encryptedKey = cryptoService.encryptAESKeyWithRSA(request.getAesKey(), request.getPublicKey());
        return ResponseEntity.ok(encryptedKey);
    }

    @PostMapping("/rsa/decrypt-key")
    public ResponseEntity<String> decryptAESKeyRSA(@RequestBody RSADecryptKeyRequest request) {
        String aesKey = cryptoService.decryptAESKeyWithRSA(request.getEncryptedKey(), request.getPrivateKey());
        return ResponseEntity.ok(aesKey);
    }
}
