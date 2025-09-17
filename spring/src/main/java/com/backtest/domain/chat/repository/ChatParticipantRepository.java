package com.backtest.repository;

import com.backtest.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 채팅 참가자 Repository
 */
@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    /**
     * 채팅방의 활성 참가자 조회
     */
    @Query("SELECT p FROM ChatParticipant p WHERE p.room.id = :roomId AND p.isActive = true")
    List<ChatParticipant> findActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    /**
     * 사용자의 채팅방 참가 정보 조회
     */
    Optional<ChatParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * 사용자의 활성 참가 정보 조회
     */
    @Query("SELECT p FROM ChatParticipant p WHERE p.room.id = :roomId AND p.user.id = :userId AND p.isActive = true")
    Optional<ChatParticipant> findActiveByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 사용자의 참가중인 채팅방 목록 조회
     */
    @Query("SELECT p FROM ChatParticipant p WHERE p.user.id = :userId AND p.isActive = true ORDER BY p.room.lastMessageAt DESC")
    List<ChatParticipant> findActiveParticipationsByUserId(@Param("userId") Long userId);

    /**
     * 채팅방의 참가자 수 조회
     */
    @Query("SELECT COUNT(p) FROM ChatParticipant p WHERE p.room.id = :roomId AND p.isActive = true")
    Long countActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    /**
     * 채팅방의 모더레이터 조회
     */
    @Query("SELECT p FROM ChatParticipant p WHERE p.room.id = :roomId AND (p.role = 'OWNER' OR p.role = 'MODERATOR') AND p.isActive = true")
    List<ChatParticipant> findModeratorsByRoomId(@Param("roomId") Long roomId);

    /**
     * 사용자가 채팅방에 참가 중인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ChatParticipant p WHERE p.room.id = :roomId AND p.user.id = :userId AND p.isActive = true")
    boolean isUserParticipating(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 참가자의 읽지 않은 메시지 수 업데이트
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatParticipant p SET p.unreadCount = p.unreadCount + 1 WHERE p.room.id = :roomId AND p.user.id != :senderId AND p.isActive = true")
    void incrementUnreadCountForOthers(@Param("roomId") Long roomId, @Param("senderId") Long senderId);

    /**
     * 사용자의 읽지 않은 메시지 수 초기화
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatParticipant p SET p.unreadCount = 0, p.lastReadMessageId = :messageId WHERE p.room.id = :roomId AND p.user.id = :userId")
    void resetUnreadCount(@Param("roomId") Long roomId, @Param("userId") Long userId, @Param("messageId") Long messageId);
}