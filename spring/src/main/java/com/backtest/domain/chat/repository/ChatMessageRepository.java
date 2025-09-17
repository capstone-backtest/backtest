package com.backtest.repository;

import com.backtest.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방의 메시지 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * 채팅방의 최근 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessagesByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * 특정 시간 이후의 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.createdAt > :afterTime AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findMessagesAfter(@Param("roomId") Long roomId, @Param("afterTime") LocalDateTime afterTime);

    /**
     * 사용자의 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.user.id = :userId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 채팅방의 메시지 수 조회
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.id = :roomId AND m.isDeleted = false")
    Long countByRoomId(@Param("roomId") Long roomId);

    /**
     * 채팅방의 최신 메시지 조회
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessageByRoomId(@Param("roomId") Long roomId, Pageable pageable);
}