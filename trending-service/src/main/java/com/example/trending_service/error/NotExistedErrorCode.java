package com.example.trending_service.error;

import com.example.trending_service.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotExistedErrorCode implements BaseErrorCode {
    USER_ALREADY_EXITS(1001, "User with this email already exists", HttpStatus.BAD_REQUEST),
    PROFILE_KEY_ALREADY_EXITS(1002, "Profile key already exists", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXITS(1002, "Profile not found for User", HttpStatus.BAD_REQUEST),
    OTP_QUEUE_EMPTY(1002, "OTP queue emmty", HttpStatus.BAD_REQUEST),
    ALREADY_FRIENDS(1002, "Already friends", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND(1002, "Friend request not found", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_NOT_FOUND(1002, "Friendship not found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1002, "User not found", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    NotExistedErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
