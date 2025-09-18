package com.backtest.repository;

import com.backtest.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅방 Repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 활성화된 공개 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r WHERE r.type = 'PUBLIC' AND r.isActive = true ORDER BY r.participantCount DESC, r.createdAt DESC")
    Page<ChatRoom> findActivePublicRooms(Pageable pageable);

    /**
     * 사용자의 채팅방 조회
     */
    @Query("SELECT r FROM ChatRoom r WHERE r.owner.id = :userId AND r.isActive = true ORDER BY r.createdAt DESC")
    Page<ChatRoom> findByOwnerId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 채팅방 검색
     */
    @Query("SELECT r FROM ChatRoom r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
           "r.type = 'PUBLIC' AND r.isActive = true")
    Page<ChatRoom> searchPublicRooms(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 사용자가 참가한 채팅방 조회
     */
    @Query("SELECT DISTINCT r FROM ChatRoom r " +
           "JOIN r.participants p " +
           "WHERE p.user.id = :userId AND p.isActive = true AND r.isActive = true " +
           "ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findRoomsByParticipantId(@Param("userId") Long userId);

    /**
     * 참가 가능한 채팅방 조회 (인원이 꽉 차지 않은)
     */
    @Query("SELECT r FROM ChatRoom r WHERE " +
           "r.type = 'PUBLIC' AND r.isActive = true AND " +
           "r.participantCount < r.maxParticipants")
    Page<ChatRoom> findAvailablePublicRooms(Pageable pageable);
}