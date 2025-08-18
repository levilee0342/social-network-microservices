package com.example.message_service.dto.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AESEncryptionRequest {
    private String plainText;
    private String key;
}
