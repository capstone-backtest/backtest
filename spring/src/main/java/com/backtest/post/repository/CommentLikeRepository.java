package com.backtest.repository;

import com.backtest.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 댓글 좋아요 Repository
 */
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 사용자의 댓글 좋아요 조회
     */
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * 사용자가 댓글에 좋아요를 눌렀는지 확인
     */
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * 댓글의 좋아요 수 조회
     */
    @Query("SELECT COUNT(l) FROM CommentLike l WHERE l.comment.id = :commentId")
    Long countByCommentId(@Param("commentId") Long commentId);

    /**
     * 사용자의 총 댓글 좋아요 수 조회
     */
    @Query("SELECT COUNT(l) FROM CommentLike l WHERE l.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}