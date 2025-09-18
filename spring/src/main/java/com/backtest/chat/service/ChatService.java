package com.backtest.chat.service;

import com.backtest.chat.dto.ChatMessageRequest;
import com.backtest.chat.dto.ChatMessageResponse;
import com.backtest.chat.dto.ChatRoomRequest;
import com.backtest.chat.dto.ChatRoomResponse;
import com.backtest.chat.entity.ChatMessage;
import com.backtest.chat.entity.ChatRoom;
import com.backtest.chat.entity.ChatRoomMember;
import com.backtest.chat.entity.ChatRoomMember.Role;
import com.backtest.chat.entity.RoomType;
import com.backtest.chat.repository.ChatMessageRepository;
import com.backtest.chat.repository.ChatRoomMemberRepository;
import com.backtest.chat.repository.ChatRoomRepository;
import com.backtest.global.exception.ApiException;
import com.backtest.global.security.UserPrincipal;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatRoomMemberRepository memberRepository,
                       ChatMessageRepository messageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<ChatRoomResponse> listRooms(RoomType type) {
        return chatRoomRepository.findActiveRooms(type).stream()
                .map(room -> new ChatRoomResponse(
                        room.getId(),
                        room.getName(),
                        room.getDescription(),
                        room.getRoomType(),
                        room.getMaxMembers(),
                        room.getCurrentMembers(),
                        room.getCreatedBy().getUsername(),
                        room.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomResponse createRoom(UserPrincipal principal, ChatRoomRequest request) {
        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        ChatRoom room = ChatRoom.create(creator, request.name(), request.description(), request.roomType(), request.maxMembers());
        chatRoomRepository.save(room);
        joinRoom(principal, room.getId(), Role.ADMIN);
        return new ChatRoomResponse(room.getId(), room.getName(), room.getDescription(), room.getRoomType(),
                room.getMaxMembers(), room.getCurrentMembers(), creator.getUsername(), room.getCreatedAt());
    }

    @Transactional
    public void joinRoom(UserPrincipal principal, Long roomId) {
        joinRoom(principal, roomId, Role.MEMBER);
    }

    @Transactional
    public void joinRoom(UserPrincipal principal, Long roomId, Role role) {
        ChatRoom room = chatRoomRepository.findByIdAndActiveTrue(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."));
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        memberRepository.findByRoomIdAndUserId(roomId, user.getId())
                .ifPresent(member -> { throw new ApiException(HttpStatus.CONFLICT, "ALREADY_JOINED", "이미 참가한 채팅방입니다."); });
        if (room.getMaxMembers() != null && room.getCurrentMembers() >= room.getMaxMembers()) {
            throw new ApiException(HttpStatus.CONFLICT, "ROOM_FULL", "채팅방 정원이 가득 찼습니다.");
        }
        room.incrementMembers();
        memberRepository.save(ChatRoomMember.join(room, user, role));
    }

    public List<ChatMessageResponse> loadRecentMessages(Long roomId, int size) {
        return messageRepository.findRecentMessages(roomId, PageRequest.of(0, size)).stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getRoom().getId(),
                        message.getMessageType(),
                        message.getContent(),
                        message.getSender().getUsername(),
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageResponse sendMessage(UserPrincipal principal, ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findByIdAndActiveTrue(request.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."));
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        memberRepository.findByRoomIdAndUserId(room.getId(), user.getId())
                .orElseGet(() -> {
                    joinRoom(principal, room.getId());
                    return null;
                });
        ChatMessage message = ChatMessage.text(room, user, request.content());
        ChatMessage saved = messageRepository.save(message);
        ChatMessageResponse response = new ChatMessageResponse(saved.getId(), room.getId(), saved.getMessageType(),
                saved.getContent(), user.getUsername(), saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), response);
        return response;
    }
}
