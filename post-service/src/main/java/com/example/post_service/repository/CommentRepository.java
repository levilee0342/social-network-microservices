package com.example.post_service.repository;

import com.example.post_service.entity.Comment;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByParentCommentOrderByCreatedAtDesc(Comment parentComment);
    List<Comment> findByPostShareOrderByCreatedAtDesc(PostShare postShare);
}