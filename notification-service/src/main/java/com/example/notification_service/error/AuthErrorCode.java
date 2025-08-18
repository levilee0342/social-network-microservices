package com.example.notification_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    UNAUTHORIZED_TO_DELETE(2002, "Unauthorized to delete this notification", HttpStatus.BAD_REQUEST),
    FAILED_TO_DELETE(2002, "Failed to delete notification", HttpStatus.BAD_REQUEST),
    NO_PERMISSION_DELETE(2002, "You do not have permission to delete this notification", HttpStatus.BAD_REQUEST),
    FAILED_MAP_NOTIFICATION(2002, "Failed to map notification", HttpStatus.BAD_REQUEST),
    FAILED_MAP_PREFERENCE(2002, "Failed to map preference user", HttpStatus.BAD_REQUEST),
    FAILED_GET_NOTIFICATION(2002, "Failed to get notification", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_NOTIFICATION(2002, "Failed to update notification", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_OR_FIND_PREFERENCE(2002, "Failed to create or find preference user", HttpStatus.BAD_REQUEST),
    FAILED_GET_OR_CREATE_PREFERENCE(2002, "Failed to get or create preference user", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_NOTIFICATION(2002, "Failed to save notification", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
