package com.example.presence_service.error;

import com.example.presence_service.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotExistedErrorCode implements BaseErrorCode {
    USER_ALREADY_EXITS(1001, "User with this email already exists", HttpStatus.BAD_REQUEST),
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    NotExistedErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
