package com.backtest.chat.controller;

import com.backtest.chat.dto.ChatMessageResponse;
import com.backtest.chat.dto.ChatRoomRequest;
import com.backtest.chat.dto.ChatMessageRequest;
import com.backtest.chat.dto.ChatRoomResponse;
import com.backtest.chat.entity.RoomType;
import com.backtest.chat.service.ChatService;
import com.backtest.global.exception.ApiException;
import com.backtest.global.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms")
    public List<ChatRoomResponse> listRooms(@RequestParam(value = "type", required = false) String type) {
        RoomType roomType = type == null ? null : RoomType.from(type);
        return chatService.listRooms(roomType);
    }

    @PostMapping("/rooms")
    public ChatRoomResponse createRoom(@Valid @RequestBody ChatRoomRequest request) {
        return chatService.createRoom(currentUser(), request);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> loadMessages(@PathVariable Long roomId,
                                                  @RequestParam(defaultValue = "50") int size) {
        return chatService.loadRecentMessages(roomId, size);
    }
    @PostMapping("/messages")
    public ChatMessageResponse sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        return chatService.sendMessage(currentUser(), request);
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
        }
        return principal;
    }
}
