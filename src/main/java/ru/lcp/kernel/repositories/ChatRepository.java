package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.lcp.kernel.entities.Chat;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserId(Integer userId);

    @Query("SELECT c.chatId FROM Chat c WHERE c.user.id IN (:user1Id, :user2Id) " +
            "GROUP BY c.chatId HAVING COUNT(DISTINCT c.user.id) = 2")
    Optional<Long> findExistingChatIdBetweenUsers(@Param("user1Id") Integer user1Id, @Param("user2Id") Integer user2Id);

    List<Chat> findByChatId(Long chatId);
}
