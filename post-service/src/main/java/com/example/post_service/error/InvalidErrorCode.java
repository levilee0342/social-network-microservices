package com.example.post_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InvalidErrorCode implements BaseErrorCode {
    INVALID_OTP(2001, "Invalid or expired OTP", HttpStatus.BAD_REQUEST),
    INVALID_KEY(2004, "Invalid key", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED(2003, "Refresh token is expired", HttpStatus.BAD_REQUEST),
    YOU_LIKED_POST(2003, "You are liked this post", HttpStatus.BAD_REQUEST),
    YOU_LIKED_COMMENT(2003, "You are liked this comment", HttpStatus.BAD_REQUEST),
    INVALID_FETCH_SHARE_P0ST(2003, "Unable to fetch shared posts", HttpStatus.BAD_REQUEST),
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
