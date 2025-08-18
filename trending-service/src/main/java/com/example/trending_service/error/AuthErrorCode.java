package com.example.trending_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    INVALID_PASSWORD(2002, "Password are incorrect", HttpStatus.BAD_REQUEST),
    MISSING_TOKEN(1004, "Missing token or token invalid", HttpStatus.UNAUTHORIZED),
    PROFILE_PRIVATE(2001, "Profile is private", HttpStatus.BAD_REQUEST),
    FAIL_GENERATE_OTP(2001, "Failed to generate OTP", HttpStatus.BAD_REQUEST),
    REDIS_NOT_ENABLE(2001, "Redis AOF persistence is not enabled", HttpStatus.BAD_REQUEST),
    SCHEDULER_NOT_RUN(2001, "Scheduler is not running. Reinitializing...", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS_REQUEST(2001, "Unauthorized access to friend request", HttpStatus.BAD_REQUEST),
    USER_CREATION_FAILED(2001, "Create account login google failed", HttpStatus.BAD_REQUEST),
    USER_LOGIN_NOT_SEND(2001, "User đang đăng nhập phải là người gửi lời mời kết bạn", HttpStatus.BAD_REQUEST),
    USER_LOGIN_ACCEPT(2001, "User đang đăng nhập phải là người chấp lời mời kết bạn", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
