package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_keys")
public class UserKey {
    @Id
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String publicKeyBase64;

    @Column(columnDefinition = "TEXT")
    private String privateKeyBase64;


}
