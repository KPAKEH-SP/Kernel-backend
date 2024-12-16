package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lcp.kernel.entities.Notification;
import ru.lcp.kernel.entities.User;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser(User user);
}
