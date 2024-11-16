package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lcp.kernel.entities.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByUserIdAndFriendId(Integer userId, Integer friendId);
    List<Friendship> findAllByUserId(Integer userId);
    List<Friendship> findAllByFriendId(Integer friendId);
}
