package com.example.message_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InvalidErrorCode implements BaseErrorCode {
    INVALID_KEY(2004, "Invalid key", HttpStatus.BAD_REQUEST),
    SENDER_ID_MISMATCH(2004, "SenderId không khớp với người dùng đang đăng nhập", HttpStatus.BAD_REQUEST),
    CREATOR_NOT_IN_PARTICIPANTS(2004, "Người tạo không nằm trong danh sách participants", HttpStatus.BAD_REQUEST),
    INVALID_GENDER(2004, "Invalid gender", HttpStatus.BAD_REQUEST),
    INVALID_BIRTH_YEAR(2004, "Invalid birth year", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(2004, "Invalid address", HttpStatus.BAD_REQUEST),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    InvalidErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
