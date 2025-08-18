package com.example.music_service.constant;

public class SpotifyConstants {

    public static final String SCOPE_USER_READ_EMAIL = "user-read-email";
    public static final String SCOPE_USER_READ_PRIVATE = "user-read-private";
    public static final String SCOPE_STREAMING = "streaming";
    public static final String SCOPE_USER_MODIFY_PLAYBACK_STATE = "user-modify-playback-state";
    public static final String SCOPE_USER_READ_PLAYBACK_STATE = "user-read-playback-state";

    public static final String SCOPES = String.join(" ",
            SCOPE_USER_READ_EMAIL,
            SCOPE_USER_READ_PRIVATE,
            SCOPE_STREAMING,
            SCOPE_USER_MODIFY_PLAYBACK_STATE,
            SCOPE_USER_READ_PLAYBACK_STATE
    );
}
