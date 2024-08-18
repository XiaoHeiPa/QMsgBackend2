package org.cubewhy.chat.repository;

import org.cubewhy.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findBySenderAndChannel(long sender, long channel, Pageable pageable);
    Page<ChatMessage> findByChannel(long channel, Pageable pageable);
}
