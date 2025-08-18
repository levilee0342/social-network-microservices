package com.example.crypto_service.service;


public interface CryptoService {
    public String encryptAES(String plainText, String base64Key);
    public String decryptAES(String cipherText, String base64Key);
    public  String encryptAESKeyWithRSA(String plainText, String base64Key);
    public  String decryptAESKeyWithRSA(String cipherText, String base64Key);
}
