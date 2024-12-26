package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.PublicFriendship;
import ru.lcp.kernel.dtos.webSocketV2.FriendPayload;
import ru.lcp.kernel.entities.Friendship;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.NotificationPatterns;
import ru.lcp.kernel.enums.WebSocketMessagesTypes;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.FriendshipRepository;
import ru.lcp.kernel.utils.UserUtils;
import ru.lcp.kernel.utils.WsMessage;
import ru.lcp.kernel.utils.WsSendMessageUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserUtils userUtils;
    private final NotificationService notificationService;
    private final WsSendMessageUtil wsSendMessageUtil;

    @Transactional
    public ResponseEntity<?> addFriend(String token, String username) {
        try {
            User user = userUtils.getByToken(token);
            User friend = userUtils.getByUsername(username);

            if (user.getId().equals(friend.getId())) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "You can't add yourself as a friend"), HttpStatus.CONFLICT);
            }

            if (friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId()).isPresent()
                    || friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId()).isPresent()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is already friend"), HttpStatus.CONFLICT);
            }

            Friendship friendship = new Friendship();
            friendship.setUser(user);
            friendship.setFriend(friend);
            friendship.setStatus("PENDING");
            friendshipRepository.save(friendship);


            FriendPayload payload = new FriendPayload();
            payload.setFriends(getPublicFriendsForUser(friend));
            WsMessage<?> message = new WsMessage<>(WebSocketMessagesTypes.NEW_FRIEND_REQUEST, payload);
            wsSendMessageUtil.sendMessageToUser(friend, message);

            //simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), getPublicFriendsForUser(friend));
            notificationService.sendNotification(user, NotificationPatterns.NEW_FRIEND_REQUEST, friend);

            return getPublicFriendsByToken(token);
        } catch (UserNotFound userNotFound) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> getPublicFriendsByToken(String token) {
        try {
            User user = userUtils.getByToken(token);

            return ResponseEntity.ok(getPublicFriendsForUser(user));
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    private List<PublicFriendship> getPublicFriendsForUser(User user) {
        List<Friendship> friends = friendshipRepository.findAllByUserId(user.getId());
        friends.addAll(friendshipRepository.findAllByFriendId(user.getId()));

        return friends.stream().map(PublicFriendship::new).toList();
    }

    @Transactional
    public ResponseEntity<?> removeFriend(String token, String username) {
        try {
            User user = userUtils.getByToken(token);
            User friend = userUtils.getByUsername(username);

            Optional<Friendship> friendshipAsUserOpt = friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId());
            Optional<Friendship> friendshipAsFriendOpt = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());

            if (friendshipAsUserOpt.isEmpty()) {
                if (friendshipAsFriendOpt.isPresent()) {
                    friendshipRepository.delete(friendshipAsFriendOpt.get());

                    return getPublicFriendsByToken(token);
                }
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is not your friend"), HttpStatus.CONFLICT);
            } else {
                friendshipRepository.delete(friendshipAsUserOpt.get());

                simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), getPublicFriendsForUser(friend));
                return getPublicFriendsByToken(token);
            }
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> acceptRequest(String token, String username) {
        try {
            User user = userUtils.getByToken(token);
            User friend = userUtils.getByUsername(username);

            Optional<Friendship> friendship = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());

            if (friendship.isEmpty()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is not your friend"), HttpStatus.CONFLICT);
            }

            friendship.get().setStatus("ACCEPTED");
            friendshipRepository.save(friendship.get());

            simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), getPublicFriendsForUser(friend));
            return getPublicFriendsByToken(token);
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}
