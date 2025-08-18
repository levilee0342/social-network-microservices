package com.example.post_service.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TokenHashedUtils {

    private static final String HASH_ALGO = "SHA-256";

    public static List<String> hashTokensFromText(String text) {
        return tokenize(text).stream()
                .map(TokenHashedUtils::sha256)
                .collect(Collectors.toList());
    }

    public static List<String> tokenize(String text) {
        String normalized = removeVietnameseDiacritics(text.toLowerCase());
        return Arrays.stream(normalized.split("\\s+"))
                .map(word -> word.replaceAll("[^\\p{L}\\p{N}]", ""))  // remove punctuation
                .filter(word -> !word.isBlank())
                .collect(Collectors.toList());
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash)
            hexString.append(String.format("%02x", b));
        return hexString.toString();
    }

    // Optional: Loại dấu tiếng Việt (nếu muốn match cả "cà phê" và "ca phe")
    public static String removeVietnameseDiacritics(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
    }
}
