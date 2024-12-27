package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.ChatParticipant;

import java.util.List;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {
    @Query("SELECT c.chat FROM ChatParticipant c WHERE c.user.id = :userId")
    List<Chat> findChatsByUserId(UUID userId);
}
