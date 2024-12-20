package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.services.FriendshipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/friends")
public class FriendsController {
    private final FriendshipService friendshipService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return friendshipService.addFriend(token, username.getUsername());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getFriends(@RequestHeader("X-Token") String token) {
        return friendshipService.getFriendsByToken(token);
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return friendshipService.removeFriend(token, username.getUsername());
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriend(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return friendshipService.acceptRequest(token, username.getUsername());
    }
}
