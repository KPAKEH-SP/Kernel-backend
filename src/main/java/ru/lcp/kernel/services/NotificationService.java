package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.entities.Notification;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.NotificationPatterns;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.NotificationRepository;
import ru.lcp.kernel.utils.UserUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserUtils userUtils;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public void sendNotification(User sender, NotificationPatterns pattern, User receiver) {
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setContent(pattern.getMessage() + sender.getUsername());

        notificationRepository.save(notification);

        simpMessagingTemplate.convertAndSend("/topic/notifications/" + receiver.getUsername(), notification.getContent());
    }

    public ResponseEntity<?> getNotifications (String token) {
        try {
            User user = userUtils.getByToken(token);
            List<String> notifications = notificationRepository.findByUser(user).stream().map(Notification::getContent).toList();

            return ResponseEntity.ok(notifications);
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}
