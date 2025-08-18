package com.example.user_service.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements BaseErrorCode {
    INVALID_PASSWORD(2002, "Password is incorrect", HttpStatus.BAD_REQUEST),
    MISSING_TOKEN(1004, "Missing or invalid token", HttpStatus.UNAUTHORIZED),
    PROFILE_PRIVATE(2001, "The profile is private", HttpStatus.BAD_REQUEST),
    FAIL_GENERATE_OTP(2001, "Failed to generate OTP", HttpStatus.BAD_REQUEST),
    REDIS_NOT_ENABLE(2001, "Redis AOF persistence is not enabled", HttpStatus.BAD_REQUEST),
    SCHEDULER_NOT_RUN(2001, "Scheduler is not running. Reinitializing...", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS_REQUEST(2001, "Unauthorized access to friend request", HttpStatus.BAD_REQUEST),
    USER_CREATION_FAILED(2001, "Failed to create account with Google login", HttpStatus.BAD_REQUEST),
    USER_LOGIN_NOT_SEND(2001, "The logged-in user must be the one sending the friend request", HttpStatus.BAD_REQUEST),
    USER_LOGIN_ACCEPT(2001, "The logged-in user must be the one accepting the friend request", HttpStatus.BAD_REQUEST),
    FAILED_GET_FRIEND_REQUEST(2001, "Failed to retrieve friend request", HttpStatus.BAD_REQUEST),
    FAILED_CHECK_PENDING_FRIEND(2001, "Failed to check pending friend request from user", HttpStatus.BAD_REQUEST),
    FAILED_GET_PENDING_FRIEND(2001, "Failed to retrieve pending friend requests from user", HttpStatus.BAD_REQUEST),
    FAILED_GET_FRIENDSHIP(2001, "Failed to retrieve friendships for user", HttpStatus.BAD_REQUEST),
    FAILED_CHECK_FRIEND(2001, "Failed to verify friendship status for user", HttpStatus.BAD_REQUEST),
    FAILED_REMOVE_FRIENDSHIP(2001, "Failed to remove friendship", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_PROFILE(2001, "Failed to create profile", HttpStatus.BAD_REQUEST),
    FAILED_GET_PROFILE(2001, "Failed to get profile", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_PROFILE(2001, "Failed to update profile", HttpStatus.BAD_REQUEST),
    FAILED_DELETE_PROFILE(2001, "Failed to delete profile", HttpStatus.BAD_REQUEST),
    FAILED_GET_ALL_PROFILE(2001, "Failed to get all profile", HttpStatus.BAD_REQUEST),
    FAILED_SEARCH_PROFILE(2001, "Failed to search profiles", HttpStatus.BAD_REQUEST),
    FAILED_MAP_PROFILE(2001, "Failed to map ProfileResponse", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_TOKEN(2001, "Failed to save tokens", HttpStatus.BAD_REQUEST),
    FAILED_GET_TOKEN(2001, "Failed to retrieve tokens", HttpStatus.BAD_REQUEST),
    FAILED_REMOVE_TOKEN(2001, "Failed to remove tokens", HttpStatus.BAD_REQUEST),
    FAILED_FIND_DAILY(2001, "Failed to find daily activities for user", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_DAILY(2001, "Failed to save daily activity for user", HttpStatus.BAD_REQUEST),
    FAILED_CHECK_USER(2001, "Failed to check existence of user", HttpStatus.BAD_REQUEST),
    FAILED_SAVE_USER(2001, "Failed to save user", HttpStatus.BAD_REQUEST),
    FAILED_GET_USER(2001, "Failed to get user", HttpStatus.BAD_REQUEST),
    FAILED_READ_TEMPLATE_EMAIL(2001, "Failed to read template send email", HttpStatus.BAD_REQUEST),
    FAILED_SEND_OTP(2001, "Failed to send OTP", HttpStatus.BAD_REQUEST),
    FAILED_LOGIN(2001, "Login failed", HttpStatus.BAD_REQUEST),
    FAILED_REFRESH_TOKEN(2001, "Failed to refresh token for user", HttpStatus.BAD_REQUEST),
    FAILED_MAP_FRIEND(2001, "Failed to map FriendResponse", HttpStatus.BAD_REQUEST),
    FAILED_GET_FRIEND(2001, "Failed to get friends for user", HttpStatus.BAD_REQUEST),
    FAILED_GET_REQUEST_PENDING(2001, "Failed to fetch pending friend requests for user", HttpStatus.BAD_REQUEST),
    FAILED_SEND_FRIEND_REQUEST(2001, "Failed to send friend request", HttpStatus.BAD_REQUEST),
    FAILED_ACCEPT_FRIEND_REQUEST(2001, "Failed to accept friend request", HttpStatus.BAD_REQUEST),
    FAILED_REJECT_FRIEND_REQUEST(2001, "Failed to reject friend request", HttpStatus.BAD_REQUEST),
    FAILED_REMOVE_FRIEND(2001, "Failed to remove friend", HttpStatus.BAD_REQUEST),
    FAILED_CHECK_IS_FRIEND(2001, "Failed to check friendship between two users", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_EDUCATION(2001, "Failed to update education", HttpStatus.BAD_REQUEST),
    FAILED_MAP_PROFILE_DETAIL(2001, "Failed to map PublicProfileDetailResponse", HttpStatus.BAD_REQUEST),
    FAILED_CREATE_PROFILE_DETAIL(2001, "Failed to create profile detail", HttpStatus.BAD_REQUEST),
    FAILED_UPDATE_PROFILE_DETAIL(2001, "Failed to update profile detail", HttpStatus.BAD_REQUEST),
    FAILED_GET_PRESENCE(2001, "Failed to get presence for users", HttpStatus.BAD_REQUEST),
    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
