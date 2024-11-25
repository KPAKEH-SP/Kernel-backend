package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lcp.kernel.dtos.AcceptFriendRequest;
import ru.lcp.kernel.dtos.AddFriendRequest;
import ru.lcp.kernel.dtos.GetFriendRequest;
import ru.lcp.kernel.dtos.RemoveFriendRequest;
import ru.lcp.kernel.services.FriendshipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/friends")
public class FriendsController {
    private final FriendshipService friendshipService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestBody AddFriendRequest addFriendRequest) {
        return friendshipService.addFriend(addFriendRequest);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getFriends(@RequestBody GetFriendRequest getFriendRequest) {
        return friendshipService.getFriends(getFriendRequest);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestBody RemoveFriendRequest removeFriendRequest) {
        return friendshipService.removeFriend(removeFriendRequest);
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriend(@RequestBody AcceptFriendRequest acceptFriendRequest) {
        return friendshipService.acceptRequest(acceptFriendRequest);
    }
}
