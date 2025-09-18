package com.backtest.repository;

import com.backtest.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 Repository
 */
@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    /**
     * 게시글의 댓글 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<PostComment> findByPostId(@Param("postId") Long postId, Pageable pageable);

    /**
     * 게시글의 루트 댓글 조회
     */
    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<PostComment> findRootCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    /**
     * 부모 댓글의 답글 조회
     */
    @Query("SELECT c FROM PostComment c WHERE c.parent.id = :parentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<PostComment> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 사용자의 댓글 조회
     */
    @Query("SELECT c FROM PostComment c WHERE c.user.id = :userId AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<PostComment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 게시글의 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.isDeleted = false")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 부모 댓글의 답글 수 조회
     */
    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.parent.id = :parentId AND c.isDeleted = false")
    Long countRepliesByParentId(@Param("parentId") Long parentId);
}