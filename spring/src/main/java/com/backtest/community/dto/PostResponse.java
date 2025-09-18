package com.backtest.community.dto;

import com.backtest.community.entity.PostCategory;
import com.backtest.community.entity.PostContentType;
import java.time.OffsetDateTime;
import java.util.List;

public record PostResponse(Long id,
                           String title,
                           String content,
                           PostCategory category,
                           PostContentType contentType,
                           long viewCount,
                           long likeCount,
                           long commentCount,
                           String author,
                           OffsetDateTime createdAt,
                           OffsetDateTime updatedAt,
                           List<CommentResponse> comments) {
}
