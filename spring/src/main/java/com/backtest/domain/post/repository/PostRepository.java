package com.backtest.repository;

import com.backtest.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 Repository
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 삭제되지 않은 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false")
    Page<Post> findAllActive(Pageable pageable);

    /**
     * 카테고리별 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    Page<Post> findByCategory(@Param("category") Post.Category category, Pageable pageable);

    /**
     * 사용자의 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.isDeleted = false")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 게시글 검색 (제목과 내용)
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.isDeleted = false")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 인기 게시글 조회 (좋아요 수 기준)
     */
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    /**
     * 고정된 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.isPinned = true AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findPinnedPosts();

    /**
     * 추천 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.isFeatured = true AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findFeaturedPosts(Pageable pageable);

    /**
     * 조회수 증가
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 게시글 상세 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);
}