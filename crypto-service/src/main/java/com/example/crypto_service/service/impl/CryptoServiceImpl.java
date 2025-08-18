package com.example.crypto_service.service.impl;

import com.example.crypto_service.service.CryptoService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class CryptoServiceImpl implements CryptoService {

    public String encryptAES(String plainText, String base64Key) {
        try {
            if (!isValidBase64(base64Key))
                throw new IllegalArgumentException("Invalid base64 AES key: " + base64Key);
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptAES(String cipherText, String base64Key) {
        try {
            if (!isValidBase64(cipherText) || !isValidBase64(base64Key))
                throw new IllegalArgumentException("Invalid base64 input");
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encryptAESKeyWithRSA(String base64AesKey, String base64PublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] encryptedKey = cipher.doFinal(Base64.getDecoder().decode(base64AesKey));
            return Base64.getEncoder().encodeToString(encryptedKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptAESKeyWithRSA(String encryptedKeyBase64, String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedKeyBase64));
            return Base64.getEncoder().encodeToString(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isValidBase64(String s) {
        return s != null && s.matches("^[A-Za-z0-9+/=]+$") && s.length() % 4 == 0;
    }
}

