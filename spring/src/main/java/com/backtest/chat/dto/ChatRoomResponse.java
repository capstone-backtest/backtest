package com.backtest.chat.dto;

import com.backtest.chat.entity.RoomType;
import java.time.OffsetDateTime;

public record ChatRoomResponse(Long id,
                               String name,
                               String description,
                               RoomType roomType,
                               Integer maxMembers,
                               Integer currentMembers,
                               String createdBy,
                               OffsetDateTime createdAt) {
}
