package com.backtest.post.dto;

import com.backtest.post.entity.Post;
import com.backtest.user.dto.UserResponse;

import java.time.LocalDateTime;

/**
 * 게시글 요약 응답 DTO (목록 조회용)
 */
public class PostSummaryResponse {

    private Long id;
    private UserResponse user;
    private Post.Category category;
    private String title;
    private String contentPreview; // 내용의 일부만
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isPinned;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public PostSummaryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public Post.Category getCategory() { return category; }
    public void setCategory(Post.Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}