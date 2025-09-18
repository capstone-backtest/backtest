package com.backtest.community.entity;

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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(name = "category", nullable = false, length = 30)
    private PostCategory category = PostCategory.GENERAL;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "content_type", nullable = false, length = 20)
    private PostContentType contentType = PostContentType.MARKDOWN;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "is_featured", nullable = false)
    private boolean featured;

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

    protected Post() {
        // JPA only
    }

    public static Post create(User author, String title, String content, PostCategory category, PostContentType contentType) {
        Post post = new Post();
        post.author = author;
        post.title = title;
        post.content = content;
        post.category = category == null ? PostCategory.GENERAL : category;
        post.contentType = contentType == null ? PostContentType.MARKDOWN : contentType;
        return post;
    }

    public void update(String title, String content, PostCategory category, PostContentType contentType) {
        this.title = title;
        this.content = content;
        this.category = category == null ? this.category : category;
        this.contentType = contentType == null ? this.contentType : contentType;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public PostCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public PostContentType getContentType() {
        return contentType;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isFeatured() {
        return featured;
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
}
