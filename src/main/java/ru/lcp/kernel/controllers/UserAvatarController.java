package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.lcp.kernel.services.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserAvatarController {
    private final UserService userService;

    @PostMapping("/avatar/upload/{userToken}")
    public ResponseEntity<?> uploadAvatar(@PathVariable String userToken, @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userToken, file);
    }

    @GetMapping("/avatar/get/{username}")
    public ResponseEntity<?> getAvatar(@PathVariable String username) {
        return userService.getAvatar(username);
    }
}