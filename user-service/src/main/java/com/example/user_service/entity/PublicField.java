package com.example.user_service.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicField<T> {
    private T value;
    private Boolean isPublic;
}
