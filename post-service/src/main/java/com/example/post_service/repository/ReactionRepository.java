package com.example.post_service.repository;

import com.example.post_service.entity.Like;
import com.example.post_service.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPost_PostIdAndUserId(Long postId, String userId);
    Optional<Like> findByComment_CommentIdAndUserId(Long commentId, String userId);
    Optional<Like> findByPostShare_ShareIdAndUserId(Long shareId, String userId);

    long countByPost_PostId(Long postId);
    // Đếm số lượng phản ứng theo loại (giữ nguyên)
    @Query("SELECT l.type AS type, COUNT(l) AS count " +
            "FROM Like l " +
            "WHERE l.post.postId = :postId " +
            "GROUP BY l.type")
    List<Object[]> countReactionsGroupByType(@Param("postId") Long postId);

    // Lấy userId và loại tương tác theo post
    @Query("SELECT l.userId, l.type " +
            "FROM Like l " +
            "WHERE l.post.postId = :postId")
    List<Object[]> findUserIdsByPostId(@Param("postId") Long postId);


    long countByComment_CommentId(Long commentId);
    @Query("SELECT l.type AS type, COUNT(l) AS count " +
            "FROM Like l " +
            "WHERE l.comment.commentId = :commentId " +
            "GROUP BY l.type")
    List<Object[]> countReactionsCommentGroupByType(@Param("commentId") Long commentId);

    long countByPostShare_ShareId(Long shareId);
    @Query("SELECT l.type AS type, COUNT(l) AS count " +
            "FROM Like l " +
            "WHERE l.postShare.shareId = :shareId " +
            "GROUP BY l.type")
    List<Object[]> countReactionsPostShareGroupByType(@Param("shareId") Long shareId);
}
