package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lcp.kernel.entities.Friendship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByUserIdAndFriendId(UUID userId, UUID friendId);
    List<Friendship> findAllByUserId(UUID userId);
    List<Friendship> findAllByFriendId(UUID friendId);
}
