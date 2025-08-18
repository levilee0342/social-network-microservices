package com.example.notification_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InvalidErrorCode implements BaseErrorCode {
    INVALID_USERID(2001, "Invalid userId format", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(2001, "Invalid token format", HttpStatus.BAD_REQUEST),
    INVALID_KEY(2004, "Invalid key", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    InvalidErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
