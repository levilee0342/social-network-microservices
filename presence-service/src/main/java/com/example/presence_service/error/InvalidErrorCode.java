package com.example.presence_service.error;

import com.example.presence_service.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InvalidErrorCode implements BaseErrorCode {
    INVALID_KEY(2004, "Invalid key", HttpStatus.BAD_REQUEST),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    InvalidErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
