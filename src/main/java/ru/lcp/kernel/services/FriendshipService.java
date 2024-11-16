package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.AddFriendRequest;
import ru.lcp.kernel.dtos.GetFriendRequest;
import ru.lcp.kernel.dtos.RemoveFriendRequest;
import ru.lcp.kernel.dtos.UserPublicInfo;
import ru.lcp.kernel.entities.Friendship;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.repositories.FriendshipRepository;
import ru.lcp.kernel.repositories.UserRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendshipService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final JwtTokenUtils jwtTokenUtils;

    @Transactional
    public ResponseEntity<?> addFriend(AddFriendRequest addFriendRequest) {
        String username = jwtTokenUtils.getUsername(addFriendRequest.getToken());

        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<User> friendOpt = userRepository.findByUsername(addFriendRequest.getFriendName());

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        if (user.getId().equals(friend.getId())) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "You can't add yourself as a friend"), HttpStatus.CONFLICT);
        }

        if (friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId()).isPresent()
                || friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId()).isPresent()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is already friend"), HttpStatus.CONFLICT);
        }

            Friendship friendship = new Friendship();
            friendship.setUser(userOpt.get());
            friendship.setFriend(friendOpt.get());
            friendshipRepository.save(friendship);

            GetFriendRequest getFriendRequest = new GetFriendRequest();
            getFriendRequest.setToken(addFriendRequest.getToken());
            return ResponseEntity.ok(getFriends(getFriendRequest));
    }

    public ResponseEntity<?> getFriends(GetFriendRequest getFriendRequest) {
        String username = jwtTokenUtils.getUsername(getFriendRequest.getToken());

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();

        List<User> friendsByUserId = friendshipRepository.findAllByUserId(user.getId()).stream()
                .map(Friendship::getFriend)
                .toList();

        List<User> friendsByFriendId = friendshipRepository.findAllByFriendId(user.getId()).stream()
                .map(Friendship::getUser)
                .toList();

        List<User> privateFriends = Stream.concat(friendsByUserId.stream(), friendsByFriendId.stream())
                .collect(Collectors.toList());

        privateFriends.remove(user);

        List<UserPublicInfo> publicFriends = new ArrayList<>();

        for (User friend : privateFriends) {
            UserPublicInfo publicFriend = new UserPublicInfo();
            publicFriend.setUsername(friend.getUsername());

            publicFriends.add(publicFriend);
        }

        return ResponseEntity.ok(publicFriends);
    }

    @Transactional
    public ResponseEntity<?> removeFriend(RemoveFriendRequest removeFriendRequest) {
        String username = jwtTokenUtils.getUsername(removeFriendRequest.getUserToken());

        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<User> friendOpt = userRepository.findByUsername(removeFriendRequest.getFriendUsername());

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        Optional<Friendship> friendshipAsUserOpt = friendshipRepository.findByUserIdAndFriendId(user.getId(), friend.getId());
        Optional<Friendship> friendshipAsFriendOpt = friendshipRepository.findByUserIdAndFriendId(friend.getId(), user.getId());

        if (friendshipAsUserOpt.isEmpty()) {
            if (friendshipAsFriendOpt.isPresent()) {
                friendshipRepository.delete(friendshipAsFriendOpt.get());

                GetFriendRequest getFriendRequest = new GetFriendRequest();
                getFriendRequest.setToken(removeFriendRequest.getUserToken());
                return ResponseEntity.ok(getFriends(getFriendRequest));
            }
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User is not your friend"), HttpStatus.CONFLICT);
        } else {
            friendshipRepository.delete(friendshipAsUserOpt.get());

            GetFriendRequest getFriendRequest = new GetFriendRequest();
            getFriendRequest.setToken(removeFriendRequest.getUserToken());
            return ResponseEntity.ok(getFriends(getFriendRequest));
        }

    }
}
