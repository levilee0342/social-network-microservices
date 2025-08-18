package com.example.presence_service.error;

import com.example.presence_service.error.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    WEBSOCKET_NOT_PRINCIPAL(2002, "Cannot determine userId, WebSocket disconnected without a principal", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_PRESENCE(2003, "Failed to update presence for connected user", HttpStatus.BAD_REQUEST),
    FAILED_HANDING_EVENT(2003, "Error handling presence event", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_REDIS(2003, "Failed to save presence to Redis", HttpStatus.BAD_REQUEST),
    FAILED_RETRIEVE_PRESENCE(2003, "Failed to retrieve user presence", HttpStatus.BAD_REQUEST),
    FAILED_DELETE_PRESENCE(2003, "Failed to delete presence from Redis", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_COUCHBASE(2003, "Failed to save presence to Couchbase", HttpStatus.BAD_REQUEST),
    FAILED_GET_PRESENCE(2003, "Failed to get presence for user", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
