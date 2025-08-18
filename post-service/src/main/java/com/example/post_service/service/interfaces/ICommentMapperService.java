package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;

public interface ICommentMapperService {
    CommentResponse mapToCommentResponse(Comment comment);
}
