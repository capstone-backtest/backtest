package com.backtest.community.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CommentResponse(Long id,
                              Long parentId,
                              String content,
                              String author,
                              long likeCount,
                              OffsetDateTime createdAt,
                              List<CommentResponse> replies) {
}
