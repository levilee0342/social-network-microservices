package com.example.post_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotExistedErrorCode implements BaseErrorCode {
    POST_NOT_EXITS(1001, "Don't find post with this useId", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_EXITS(1001, "Don't find this comment", HttpStatus.BAD_REQUEST),
    POST_ALREADY_SHARED(1002, "Post already shared with this user", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    NotExistedErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
