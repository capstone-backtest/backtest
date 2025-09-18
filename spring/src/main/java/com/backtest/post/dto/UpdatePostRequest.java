package com.backtest.post.dto;

import jakarta.validation.constraints.Size;

/**
 * 게시글 수정 요청 DTO
 */
public class UpdatePostRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    private String content;

    // 기본 생성자
    public UpdatePostRequest() {}

    // 생성자
    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}