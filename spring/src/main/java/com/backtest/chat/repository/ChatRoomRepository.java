package com.backtest.chat.repository;

import com.backtest.chat.entity.ChatRoom;
import com.backtest.chat.entity.RoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select r from ChatRoom r where r.active = true and (:type is null or r.roomType = :type) order by r.createdAt desc")
    List<ChatRoom> findActiveRooms(RoomType type);

    Optional<ChatRoom> findByIdAndActiveTrue(Long id);
}
