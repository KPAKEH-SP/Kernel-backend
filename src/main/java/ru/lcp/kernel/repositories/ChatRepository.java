package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lcp.kernel.entities.Chat;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    @Query("SELECT c " +
            "FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE p1.user.id = :userId1 AND p2.user.id = :userId2")
    Optional<Chat> findChatByParticipants(@Param("userId1") UUID participantId1, @Param("userId2") UUID participantId2);
}
