package com.backtest.chat.entity;

import com.backtest.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 채팅방 엔티티
 */
@Entity
@Table(name = "chat_rooms")
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type = RoomType.PUBLIC;

    @Column(name = "max_participants")
    private Integer maxParticipants = 100;

    @Column(name = "participant_count")
    private Integer participantCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "password_protected")
    private Boolean passwordProtected = false;

    @Size(max = 255)
    @Column(name = "password_hash")
    private String passwordHash;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    // 연관 관계
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatMessage> messages = new HashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatParticipant> participants = new HashSet<>();

    // 기본 생성자
    public ChatRoom() {}

    // 생성자
    public ChatRoom(User owner, String name, RoomType type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getPasswordProtected() { return passwordProtected; }
    public void setPasswordProtected(Boolean passwordProtected) { this.passwordProtected = passwordProtected; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Set<ChatMessage> getMessages() { return messages; }
    public void setMessages(Set<ChatMessage> messages) { this.messages = messages; }

    public Set<ChatParticipant> getParticipants() { return participants; }
    public void setParticipants(Set<ChatParticipant> participants) { this.participants = participants; }

    // 비즈니스 메서드
    public void incrementParticipantCount() {
        this.participantCount++;
    }

    public void decrementParticipantCount() {
        if (this.participantCount > 0) {
            this.participantCount--;
        }
    }

    public boolean isFull() {
        return participantCount >= maxParticipants;
    }

    public boolean canJoin() {
        return isActive && !isFull();
    }

    /**
     * 채팅방 타입 enum
     */
    public enum RoomType {
        PUBLIC,         // 공개
        PRIVATE,        // 비공개
        DIRECT_MESSAGE  // 1:1 채팅
    }
}