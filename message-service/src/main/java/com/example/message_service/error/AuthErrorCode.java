package com.example.message_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    CONVERSATION_INVALID_PARTICIPANTS(2002, "Cuộc trò chuyện cần ít nhất 2 thành viên", HttpStatus.BAD_REQUEST),
    CREATE_CONVERSATION_FAILED(2002, "Tạo cuộc trò chuyện thất bại", HttpStatus.BAD_REQUEST),
    USER_NOT_FRIEND(2002, "Tất cả người tham gia phải là bạn bè", HttpStatus.BAD_REQUEST),
    SEND_MESSAGE_FAILED(2002, "Gửi tin nhắn thất bại", HttpStatus.BAD_REQUEST),
    MARK_AS_READ_FAILED(2002, "Đánh dấu tin nhắn đã đọc thất bại", HttpStatus.BAD_REQUEST),
    DELETE_MESSAGE_FAILED(2002, "Xóa tin nhắn thất bại", HttpStatus.BAD_REQUEST),
    BLOCKED_BY_USER(2002, "Bạn đã bị chặn, không thể gửi tin nhắn!", HttpStatus.BAD_REQUEST),
    BLOCK_BY_USER(2002, "Không thể gửi tin nhắn, bạn đã chặn người này!", HttpStatus.BAD_REQUEST),
    PROCESS_MESS_FAILED(2002, "Failed to process message", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_REDIS(2002, "Failed save to redis", HttpStatus.BAD_REQUEST),
    FAILED_RETRIEVE_REDIS(2002, "Failed retrieve redis", HttpStatus.BAD_REQUEST),
    REQUEST_RECEIVED(2002, "Request received, searching for a match...", HttpStatus.CREATED),
    FAILED_RETRIEVE_PRESENCE(2002, "Failed to retrieve presence in Redis", HttpStatus.BAD_REQUEST),
    GET_MESSAGES_FAILED(2002,"Failed to get messages for conversation", HttpStatus.BAD_REQUEST),
    FETCH_CONVERSATION_FAILED(2002,"Failed to fetch conversation", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
