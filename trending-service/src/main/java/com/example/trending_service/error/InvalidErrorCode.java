package com.example.trending_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InvalidErrorCode implements BaseErrorCode {
    INVALID_OTP(2001, "Invalid or expired OTP", HttpStatus.BAD_REQUEST),
    INVALID_KEY(2004, "Invalid key", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED(2003, "Refresh token is expired", HttpStatus.BAD_REQUEST),
    INVALID_FRIEND_REQUEST(2003, "Invalid friend request", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_PENDING(2003, "Friend request is pending", HttpStatus.BAD_REQUEST),
    INVALID_FRIEND_REQUEST_STATUS(2003, "Invalid friend request status", HttpStatus.BAD_REQUEST),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    InvalidErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
