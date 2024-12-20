package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lcp.kernel.entities.ChatMessage;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByChatIdOrderByTimestamp(UUID chatId);
}