package com.example.search_service.service.interfaces;

import com.example.search_service.dto.request.PostEvent;

public interface IPostEventHandler {
    void handle(PostEvent event);
}
