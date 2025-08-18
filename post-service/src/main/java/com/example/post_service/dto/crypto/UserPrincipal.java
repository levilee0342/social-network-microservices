package com.example.post_service.dto.crypto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    private String id;
    private String publicKeyBase64;
    private String privateKeyBase64;

}