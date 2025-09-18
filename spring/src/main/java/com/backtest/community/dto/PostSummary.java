package com.backtest.community.dto;

import com.backtest.community.entity.PostCategory;
import java.time.OffsetDateTime;

public record PostSummary(Long id,
                          String title,
                          String author,
                          PostCategory category,
                          long viewCount,
                          long likeCount,
                          long commentCount,
                          OffsetDateTime createdAt) {
}
