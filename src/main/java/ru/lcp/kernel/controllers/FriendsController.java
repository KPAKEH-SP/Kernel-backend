package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.services.FriendshipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/friends")
public class FriendsController {
    private final FriendshipService friendshipService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestBody TokenAndFriendUsername tokenAndFriendUsername) {
        return friendshipService.addFriend(tokenAndFriendUsername);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getFriends(@RequestBody GetFriendRequest getFriendRequest) {
        return friendshipService.getFriends(getFriendRequest);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestBody TokenAndFriendUsername tokenAndFriendUsername) {
        return friendshipService.removeFriend(tokenAndFriendUsername);
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriend(@RequestBody TokenAndFriendUsername tokenAndFriendUsername) {
        return friendshipService.acceptRequest(tokenAndFriendUsername);
    }
}
