package com.example.message_service.dto.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSAEncryptKeyRequest {
    private String aesKey;
    private String publicKey;
}
