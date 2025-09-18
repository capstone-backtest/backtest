package com.backtest.domain.post.dto;

import com.backtest.domain.post.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 게시글 생성 요청 DTO
 */
public class CreatePostRequest {

    @NotNull(message = "카테고리는 필수입니다")
    private Post.Category category;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    private Post.ContentType contentType = Post.ContentType.MARKDOWN;

    // 기본 생성자
    public CreatePostRequest() {}

    // 생성자
    public CreatePostRequest(Post.Category category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    // Getters and Setters
    public Post.Category getCategory() { return category; }
    public void setCategory(Post.Category category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Post.ContentType getContentType() { return contentType; }
    public void setContentType(Post.ContentType contentType) { this.contentType = contentType; }
}