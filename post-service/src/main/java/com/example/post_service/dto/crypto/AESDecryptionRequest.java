package com.example.post_service.dto.crypto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AESDecryptionRequest {
    private String cipherText;
    private String key;
}
