package com.backtest.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.backtest.auth.dto.RegisterRequest;
import com.backtest.auth.service.AuthService;
import com.backtest.chat.dto.ChatMessageRequest;
import com.backtest.chat.dto.ChatMessageResponse;
import com.backtest.chat.dto.ChatRoomRequest;
import com.backtest.chat.dto.ChatRoomResponse;
import com.backtest.chat.entity.ChatMessageType;
import com.backtest.chat.entity.RoomType;
import com.backtest.global.security.UserPrincipal;
import com.backtest.user.entity.InvestmentType;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        if (principal == null) {
            authService.register(new RegisterRequest("chatter", "chat@example.com", "Password123", InvestmentType.BALANCED),
                    "JUnit", "127.0.0.1");
            User user = userRepository.findByEmail("chat@example.com").orElseThrow();
            principal = UserPrincipal.from(user);
        }
    }

    @Test
    void createRoomAndSendMessage() {
        ChatRoomRequest request = new ChatRoomRequest("테스트룸", "설명", RoomType.PUBLIC, 100);
        ChatRoomResponse room = chatService.createRoom(principal, request);
        assertThat(room.name()).isEqualTo("테스트룸");

        ChatMessageResponse response = chatService.sendMessage(principal,
                new ChatMessageRequest(room.id(), "안녕하세요", ChatMessageType.TEXT));
        assertThat(response.content()).isEqualTo("안녕하세요");
        assertThat(response.roomId()).isEqualTo(room.id());
    }
}
