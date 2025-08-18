package com.example.message_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotExistedErrorCode implements BaseErrorCode {
    USER_ALREADY_EXITS(1001, "User with this email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1001, "Người dùng không tồn tại", HttpStatus.BAD_REQUEST),
    CONVERSATION_NOT_FOUND(1001, "Không tìm thấy cuộc trò chuyện", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND(1001, "Không tìm thấy tin nhắn", HttpStatus.BAD_REQUEST),
    USER_NOT_IN_CONVERSATION(1001, "Người dùng không thuộc đoạn chat", HttpStatus.BAD_REQUEST),
    USER_NOT_ONLINE(1001, "User not online", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    NotExistedErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
