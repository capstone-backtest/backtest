package com.backtest.chat.dto;

import com.backtest.chat.entity.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequest(
        @NotNull(message = "채팅방 ID를 입력해주세요.")
        Long roomId,
        @NotBlank(message = "메시지 내용을 입력해주세요.")
        String content,
        ChatMessageType messageType
) {
}
