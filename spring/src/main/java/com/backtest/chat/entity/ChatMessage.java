package com.backtest.chat.entity;

import com.backtest.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User sender;

    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Integer fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private ChatMessage replyTo;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ChatMessage() {
        // JPA only
    }

    public static ChatMessage text(ChatRoom room, User sender, String content) {
        ChatMessage message = new ChatMessage();
        message.room = room;
        message.sender = sender;
        message.content = content;
        message.messageType = ChatMessageType.TEXT;
        return message;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public User getSender() {
        return sender;
    }

    public ChatMessageType getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public ChatMessage getReplyTo() {
        return replyTo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
