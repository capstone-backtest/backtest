package com.backtest.chat.dto;

import com.backtest.chat.entity.ChatMessageType;
import java.time.OffsetDateTime;

public record ChatMessageResponse(Long id,
                                  Long roomId,
                                  ChatMessageType messageType,
                                  String content,
                                  String sender,
                                  OffsetDateTime createdAt) {
}
