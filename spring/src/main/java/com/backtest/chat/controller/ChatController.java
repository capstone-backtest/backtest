package com.backtest.chat.controller;

import com.backtest.chat.dto.*;
import com.backtest.chat.entity.ChatRoom;
import com.backtest.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "채팅 관리 API")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(
            @Valid @RequestBody CreateChatRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatRoomResponse room = chatService.createRoom(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공")
    @GetMapping("/rooms")
    public ResponseEntity<Page<ChatRoomSummaryResponse>> getRooms(
            @Parameter(description = "채팅방 타입") @RequestParam(required = false) ChatRoom.RoomType type,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String search,
            @Parameter(description = "활성 채팅방만 조회") @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        Page<ChatRoomSummaryResponse> rooms = chatService.getRooms(type, search, activeOnly, pageable);
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "채팅방 상세 조회", description = "채팅방 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 조회 성공"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId) {
        ChatRoomResponse room = chatService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "채팅방 입장", description = "채팅방에 입장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 입장 성공"),
        @ApiResponse(responseCode = "403", description = "입장 권한 없음"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @RequestBody(required = false) JoinRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.joinRoom(roomId, userDetails.getUsername(), request != null ? request.getPassword() : null);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "채팅방 퇴장", description = "채팅방에서 퇴장합니다.")
    @ApiResponse(responseCode = "200", description = "채팅방 퇴장 성공")
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.leaveRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "채팅 메시지 조회", description = "채팅방의 메시지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "메시지 조회 성공")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<ChatMessageResponse> messages = chatService.getMessages(roomId, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "내 채팅방 목록", description = "현재 사용자가 참여한 채팅방 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 채팅방 목록 조회 성공")
    @GetMapping("/rooms/my")
    public ResponseEntity<List<ChatRoomSummaryResponse>> getMyRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ChatRoomSummaryResponse> rooms = chatService.getUserRooms(userDetails.getUsername());
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "채팅방 설정 수정", description = "채팅방 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "설정 수정 성공"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> updateRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Valid @RequestBody UpdateChatRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatRoomResponse room = chatService.updateRoom(roomId, request, userDetails.getUsername());
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "메시지 읽음 표시", description = "메시지를 읽음으로 표시합니다.")
    @ApiResponse(responseCode = "200", description = "읽음 표시 성공")
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @RequestBody MarkAsReadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.markAsRead(roomId, userDetails.getUsername(), request.getLastMessageId());
        return ResponseEntity.ok().build();
    }
}

@Controller
class ChatWebSocketController {

    private final ChatService chatService;

    @Autowired
    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        chatService.sendMessage(request, username);
    }

    @MessageMapping("/chat.join")
    public void joinRoom(@Payload JoinRoomWebSocketRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        chatService.handleWebSocketJoin(request.getRoomId(), username);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload LeaveRoomWebSocketRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        chatService.handleWebSocketLeave(request.getRoomId(), username);
    }
}
