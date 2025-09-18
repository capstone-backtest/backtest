package com.backtest.chat.entity;

import com.backtest.user.entity.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅방 참가자 엔티티
 */
@Entity
@Table(name = "chat_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    // 기본 생성자
    public ChatParticipant() {}

    // 생성자
    public ChatParticipant(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

    // 관리자 생성자
    public ChatParticipant(ChatRoom room, User user, Role role) {
        this.room = room;
        this.user = user;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    // 비즈니스 메서드
    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    public void leave() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        this.isActive = true;
        this.leftAt = null;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    public boolean isModerator() {
        return role == Role.MODERATOR || role == Role.OWNER;
    }

    /**
     * 참가자 역할 enum
     */
    public enum Role {
        OWNER,          // 방장
        MODERATOR,      // 모더레이터
        MEMBER          // 일반 멤버
    }
}