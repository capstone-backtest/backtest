package com.backtest.repository;

import com.backtest.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 좋아요 Repository
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 사용자의 게시글 좋아요 조회
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 사용자가 게시글에 좋아요를 눌렀는지 확인
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 게시글의 좋아요 수 조회
     */
    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 사용자의 총 좋아요 수 조회
     */
    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}