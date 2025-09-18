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
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType = RoomType.PUBLIC;

    @Column(name = "max_members")
    private Integer maxMembers = 100;

    @Column(name = "current_members")
    private Integer currentMembers = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ChatRoom() {
        // JPA only
    }

    public static ChatRoom create(User creator, String name, String description, RoomType roomType, Integer maxMembers) {
        ChatRoom room = new ChatRoom();
        room.createdBy = creator;
        room.name = name;
        room.description = description;
        room.roomType = roomType == null ? RoomType.PUBLIC : roomType;
        room.maxMembers = maxMembers == null ? 100 : maxMembers;
        return room;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public Integer getCurrentMembers() {
        return currentMembers;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void incrementMembers() {
        if (currentMembers == null) {
            currentMembers = 0;
        }
        currentMembers++;
    }

    public void decrementMembers() {
        if (currentMembers != null && currentMembers > 0) {
            currentMembers--;
        }
    }

    public void close() {
        this.active = false;
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
