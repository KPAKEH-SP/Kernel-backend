package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.PublicChat;
import ru.lcp.kernel.dtos.PublicUser;
import ru.lcp.kernel.entities.PersonalChat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.PersonalChatRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;
import ru.lcp.kernel.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalChatService {
    private final PersonalChatRepository personalChatRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final UserUtils userUtils;

    @Transactional
    public ResponseEntity<?> createPersonalChat(String token, String username) {
        try {
            User user = userUtils.getByToken(token);
            User friend = userUtils.getByUsername(username);

            List<PersonalChat> personalChats = personalChatRepository.findByFirstUser(user);
            personalChats.addAll(personalChatRepository.findBySecondUser(user));

            List<PersonalChat> allChats = personalChats.stream().distinct().toList();

            for (PersonalChat personalChat : allChats) {
                if (personalChat.getFirstUser().equals(friend) || personalChat.getSecondUser().equals(friend)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(personalChat.getChatId());
                }
            }

            PersonalChat newPersonalChat = new PersonalChat();
            newPersonalChat.setFirstUser(user);
            newPersonalChat.setSecondUser(friend);

            personalChatRepository.save(newPersonalChat);

            return getPersonalChatsForUserById(user);
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getPersonalChatsForUserById(User user) {
        List<PersonalChat> personalChats = personalChatRepository.findByFirstUser(user);
        personalChats.addAll(personalChatRepository.findBySecondUser(user));

        List<PersonalChat> allChats = personalChats.stream().distinct().toList();

        List<PublicChat> publicPersonalChats = allChats.stream().map(personalChat -> {
            List<PublicUser> chatUsers = new ArrayList<>();
            chatUsers.add(new PublicUser(personalChat.getFirstUser()));
            chatUsers.add(new PublicUser(personalChat.getSecondUser()));

            PublicChat publicPersonalChat = new PublicChat();
            publicPersonalChat.setUsers(chatUsers);
            publicPersonalChat.setChatId(personalChat.getChatId());
            return publicPersonalChat;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(publicPersonalChats);
    }

    public ResponseEntity<?> getPersonalChatsForUserByToken(String token) {
        String username = jwtTokenUtils.getUsername(token);
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        return getPersonalChatsForUserById(user);
    }

    public PersonalChat getPersonalChat(UUID chatId) {
        return personalChatRepository.findById(chatId).orElse(null);
    }
}
