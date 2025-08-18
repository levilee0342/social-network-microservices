package com.example.music_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    NO_PERMISSION_EDIT_POST(2002, "You do not have permission to edit this post", HttpStatus.BAD_REQUEST),
    NO_PERMISSION_DELETE_POST(2003, "You do not have permission to delete this post", HttpStatus.BAD_REQUEST),
    CAN_NOT_CREATE_POST(2003, "Can't create post", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
