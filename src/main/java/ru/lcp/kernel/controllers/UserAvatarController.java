package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.lcp.kernel.services.AvatarService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserAvatarController {
    private final AvatarService avatarService;

    @PostMapping("/avatar/upload")
    public ResponseEntity<?> uploadAvatar(@RequestHeader(name = "X-Token") String userToken, @RequestParam("file") MultipartFile file) {
        return avatarService.uploadAvatar(userToken, file);
    }

    @GetMapping("/avatar/get/{username}")
    public ResponseEntity<?> getAvatar(@PathVariable String username) {
        return avatarService.getAvatar(username);
    }
}