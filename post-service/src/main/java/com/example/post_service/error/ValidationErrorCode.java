package com.example.post_service.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ValidationErrorCode implements BaseErrorCode {
    CONTENT_TOO_LONG(400, "Content must be ≤ 150 characters"),
    VOICE_TOO_LONG(400, "Voice snippet must be ≤ 10 seconds");

    private final int code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }
}
