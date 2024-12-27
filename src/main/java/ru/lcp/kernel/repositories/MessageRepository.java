package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.Message;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByChatOrderByCreatedAt(Chat chat);
}