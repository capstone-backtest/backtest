package com.backtest.chat.entity;

import com.backtest.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 */
@Entity
@Table(name = "chat_messages",
       indexes = {
           @Index(name = "idx_chat_room_created", columnList = "room_id, created_at"),
           @Index(name = "idx_chat_user_created", columnList = "user_id, created_at")
       })
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @Size(max = 500)
    @Column(name = "metadata_json")
    private String metadataJson;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public ChatMessage() {}

    // 생성자
    public ChatMessage(ChatRoom room, User user, String content) {
        this.room = room;
        this.user = user;
        this.content = content;
    }

    // 시스템 메시지 생성자
    public ChatMessage(ChatRoom room, User user, String content, MessageType messageType) {
        this.room = room;
        this.user = user;
        this.content = content;
        this.messageType = messageType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // 비즈니스 메서드
    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM || messageType == MessageType.JOIN || messageType == MessageType.LEAVE;
    }

    /**
     * 메시지 타입 enum
     */
    public enum MessageType {
        TEXT,           // 텍스트 메시지
        IMAGE,          // 이미지
        FILE,           // 파일
        SYSTEM,         // 시스템 메시지
        JOIN,           // 입장 알림
        LEAVE           // 퇴장 알림
    }
}