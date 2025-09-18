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

@Entity
@Table(name = "chat_room_members")
public class ChatRoomMember {

    public enum Role {
        MEMBER,
        MODERATOR,
        ADMIN;

        public static Role from(String value) {
            if (value == null || value.isBlank()) {
                return MEMBER;
            }
            return Role.valueOf(value.toUpperCase());
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "last_read_at")
    private OffsetDateTime lastReadAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected ChatRoomMember() {
        // JPA only
    }

    public static ChatRoomMember join(ChatRoom room, User user, Role role) {
        ChatRoomMember member = new ChatRoomMember();
        member.room = room;
        member.user = user;
        member.role = role == null ? Role.MEMBER : role;
        return member;
    }

    public Long getId() {
        return id;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public OffsetDateTime getLastReadAt() {
        return lastReadAt;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
        this.lastReadAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
