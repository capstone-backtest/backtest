package com.backtest.chat.repository;

import com.backtest.chat.entity.ChatRoomMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);
    List<ChatRoomMember> findByRoomId(Long roomId);
    long countByRoomId(Long roomId);
}
