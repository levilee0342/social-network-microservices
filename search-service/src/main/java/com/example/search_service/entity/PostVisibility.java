package com.example.search_service.entity;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PostVisibility {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE,
    ANONYMOUS
}