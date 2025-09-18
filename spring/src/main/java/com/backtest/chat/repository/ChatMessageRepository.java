package com.backtest.chat.repository;

import com.backtest.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("select m from ChatMessage m where m.room.id = :roomId and m.deleted = false order by m.createdAt desc")
    List<ChatMessage> findRecentMessages(Long roomId, Pageable pageable);
}
