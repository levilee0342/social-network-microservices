package com.example.post_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    NO_PERMISSION_EDIT_POST(2002, "You do not have permission to edit this post", HttpStatus.BAD_REQUEST),
    NO_PERMISSION_DELETE_POST(2003, "You do not have permission to delete this post", HttpStatus.BAD_REQUEST),
    CAN_NOT_CREATE_POST(2003, "Can't create post", HttpStatus.BAD_REQUEST),
    ACCESS_NOT_VIEW_POST(2003, "You are not allowed to view this post", HttpStatus.BAD_REQUEST),
    ACCESS_NOT_EDIT_COMMENT(2003, "You are not allowed to edit this comment", HttpStatus.BAD_REQUEST),
    ACCESS_NOT_DELETE_COMMENT(2003, "You are not allowed to delete this comment", HttpStatus.BAD_REQUEST),
    FAILED_GET_POST_HISTORY(2003, "Error getting post history for this post", HttpStatus.BAD_REQUEST),
    FAILED_GET_USER_ACTIVITY(2003, "Error getting user activity for user", HttpStatus.BAD_REQUEST),
    FAILED_GET_EVENT(2003, "Error getting events by this type", HttpStatus.BAD_REQUEST),
    FAILED_GET_EVENT_IN_DAY(2003, "Error getting events from startDate to endDate", HttpStatus.BAD_REQUEST),
    FAILED_GET_POST_STATE(2003, "Error retrieving post state at time", HttpStatus.BAD_REQUEST),
    FAILED_MAP_COMMENT(2003, "Failed to map comment", HttpStatus.BAD_REQUEST),
    FAILED_GET_COMMENT(2003, "Failed to get all comment", HttpStatus.BAD_REQUEST),
    FAILED_GET_REPLY_COMMENT(2003, "Failed to get all reply comment", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_COMMENT(2003, "Failed to create comment", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_REPLY_COMMENT(2003, "Failed to create reply comment", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_COMMENT(2003, "Failed to update comment", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_REPLY_COMMENT(2003, "Failed to update reply comment", HttpStatus.BAD_REQUEST),
    FAILED_DELETE_COMMENT(2003, "Failed to delete comment", HttpStatus.BAD_REQUEST),
    FAILED_MAP_NEW_FEED(2003, "Failed to map post new feed", HttpStatus.BAD_REQUEST),
    FAILED_PARSE_JSON(2003, "Failed to parse Json", HttpStatus.BAD_REQUEST),
    FAILED_MAP_POST(2003, "Failed to map post", HttpStatus.BAD_REQUEST),
    FAILED_GET_POST(2003, "Failed to get post", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_POST(2003, "Failed to create post", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_POST(2003, "Failed to update post", HttpStatus.BAD_REQUEST),
    FAILED_DELETE_POST(2003, "Failed to delete post", HttpStatus.BAD_REQUEST),
    FAILED_COUNT_REACTION(2003, "Failed to count reaction for post", HttpStatus.BAD_REQUEST),
    FAILED_REACTION_COMMENT(2003, "Failed to reaction for comment", HttpStatus.BAD_REQUEST),
    FAILED_SEARCH_ELASTICSEARCH(2003, "Failed to search content by query text on elasticsearch", HttpStatus.BAD_REQUEST),
    FAILED_SEARCH_POST(2003, "Failed to search post by query", HttpStatus.BAD_REQUEST),;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
