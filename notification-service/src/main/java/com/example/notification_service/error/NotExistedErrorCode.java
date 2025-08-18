package com.example.notification_service.error;

import com.example.notification_service.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotExistedErrorCode implements BaseErrorCode {
    USER_ALREADY_EXITS(1001, "User with this email already exists", HttpStatus.BAD_REQUEST),
    PROFILE_KEY_ALREADY_EXITS(1002, "Profile key already exists", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXITS(1002, "Profile not found for User", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(1002, "Don't find notifications", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    NotExistedErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
