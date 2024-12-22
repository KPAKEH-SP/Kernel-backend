package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.Username;
import ru.lcp.kernel.services.PersonalChatService;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/chats/personal")
public class PersonalChatController {
    private final PersonalChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createPersonalChat(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return chatService.createPersonalChat(token, username.getUsername());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllPersonalChats(@RequestHeader("X-Token") String token) {
        return chatService.getPersonalChatsForUserByToken(token);
    }
}
