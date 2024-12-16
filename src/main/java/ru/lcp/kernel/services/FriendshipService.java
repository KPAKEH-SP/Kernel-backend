package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.entities.Friendship;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.NotificationPatterns;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.FriendshipRepository;
import ru.lcp.kernel.repositories.UserRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;
import ru.lcp.kernel.utils.PrivateUserConvertor;
import ru.lcp.kernel.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final PrivateUserConvertor privateUserConvertor;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserUtils userUtils;
    private final NotificationService notificationService;

    @Transactional
    public ResponseEntity<?> addFriend(TokenAndFriendUsername tokenAndFriendUsername) {
        try {
            User user = userUtils.getByToken(tokenAndFriendUsername.getToken());
            User friend = userUtils.getByUsername(tokenAndFriendUsername.getFriendUsername());

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

            GetFriendRequest getFriendRequest = new GetFriendRequest();
            getFriendRequest.setToken(tokenAndFriendUsername.getToken());

            simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), "new friend request");
            notificationService.sendNotification(user, NotificationPatterns.NEW_FRIEND_REQUEST, friend);

            return ResponseEntity.ok(getFriends(getFriendRequest));
        } catch (UserNotFound userNotFound) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getFriends(GetFriendRequest getFriendRequest) {
        String username = jwtTokenUtils.getUsername(getFriendRequest.getToken());

        return getFriends(username);
    }

    public ResponseEntity<?> getFriends(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();

        List<Friendship> friendsByUserId = friendshipRepository.findAllByUserId(user.getId());

        List<Friendship> friendsByFriendId = friendshipRepository.findAllByFriendId(user.getId());

        List<PublicFriendship> publicFriends = new ArrayList<>();

        for (Friendship friendship : friendsByFriendId) {
            PublicFriendship publicFriendship = new PublicFriendship();
            UserPublicInfo publicFriend = privateUserConvertor.convertUserPublicInfo(friendship.getUser());
            publicFriendship.setUser(publicFriend);
            publicFriendship.setStatus(friendship.getStatus());
            if (friendship.getStatus().equals("PENDING")) {
                UserPublicInfo pendingFrom = privateUserConvertor.convertUserPublicInfo(friendship.getUser());
                publicFriendship.setPendingFrom(pendingFrom);
            }
            publicFriends.add(publicFriendship);
        }

        for (Friendship friendship : friendsByUserId) {
            PublicFriendship publicFriendship = new PublicFriendship();
            UserPublicInfo publicFriend = privateUserConvertor.convertUserPublicInfo(friendship.getFriend());
            publicFriendship.setUser(publicFriend);
            publicFriendship.setStatus(friendship.getStatus());
            if (friendship.getStatus().equals("PENDING")) {
                UserPublicInfo pendingFrom = privateUserConvertor.convertUserPublicInfo(friendship.getUser());
                publicFriendship.setPendingFrom(pendingFrom);
            }
            publicFriends.add(publicFriendship);
        }

        return ResponseEntity.ok(publicFriends);
    }

    @Transactional
    public ResponseEntity<?> removeFriend(TokenAndFriendUsername tokenAndFriendUsername) {
        try {
            User user = userUtils.getByToken(tokenAndFriendUsername.getToken());
            User friend = userUtils.getByUsername(tokenAndFriendUsername.getFriendUsername());

            Optional<Friendship> friendshipAsUserOpt = friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId());
            Optional<Friendship> friendshipAsFriendOpt = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());

            if (friendshipAsUserOpt.isEmpty()) {
                if (friendshipAsFriendOpt.isPresent()) {
                    friendshipRepository.delete(friendshipAsFriendOpt.get());

                    GetFriendRequest getFriendRequest = new GetFriendRequest();
                    getFriendRequest.setToken(tokenAndFriendUsername.getToken());
                    return ResponseEntity.ok(getFriends(getFriendRequest));
                }
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is not your friend"), HttpStatus.CONFLICT);
            } else {
                friendshipRepository.delete(friendshipAsUserOpt.get());

                GetFriendRequest getFriendRequest = new GetFriendRequest();
                getFriendRequest.setToken(tokenAndFriendUsername.getToken());
                simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), "friend/request removed");
                return ResponseEntity.ok(getFriends(getFriendRequest));
            }
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> acceptRequest(TokenAndFriendUsername tokenAndFriendUsername) {
        try {
            User user = userUtils.getByToken(tokenAndFriendUsername.getToken());
            User friend = userUtils.getByUsername(tokenAndFriendUsername.getFriendUsername());

            Optional<Friendship> friendship = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());

            if (friendship.isEmpty()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is not your friend"), HttpStatus.CONFLICT);
            }

            friendship.get().setStatus("ACCEPTED");
            friendshipRepository.save(friendship.get());

            GetFriendRequest getFriendRequest = new GetFriendRequest();
            getFriendRequest.setToken(tokenAndFriendUsername.getToken());
            simpMessagingTemplate.convertAndSend("/topic/requests/friend/" + friend.getUsername(), "friend request accepted");
            return ResponseEntity.ok(getFriends(getFriendRequest));
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}
