package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lcp.kernel.entities.Chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByUserId(UUID userId);

    @Query("SELECT c.id FROM Chat c WHERE c.user.id IN (:user1Id, :user2Id) " +
            "GROUP BY c.id HAVING COUNT(DISTINCT c.user.id) = 2")
    Optional<UUID> findExistingChatIdBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);

    List<Chat> findAllById(UUID id);
}
