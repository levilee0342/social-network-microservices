package com.example.message_service.error;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    int getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
