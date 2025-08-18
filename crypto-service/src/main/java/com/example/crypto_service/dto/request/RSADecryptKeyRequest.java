package com.example.crypto_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSADecryptKeyRequest {
    private String encryptedKey;
    private String privateKey;


}