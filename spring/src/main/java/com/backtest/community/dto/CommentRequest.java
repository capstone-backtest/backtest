package com.backtest.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content,
        Long parentId
) {
}
