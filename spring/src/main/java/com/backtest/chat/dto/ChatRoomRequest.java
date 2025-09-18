package com.backtest.chat.dto;

import com.backtest.chat.entity.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRoomRequest(
        @NotBlank(message = "채팅방 이름을 입력해주세요.")
        @Size(max = 100, message = "채팅방 이름은 100자 이하로 입력해주세요.")
        String name,
        @Size(max = 500, message = "설명은 500자 이하로 입력해주세요.")
        String description,
        RoomType roomType,
        Integer maxMembers
) {
}
