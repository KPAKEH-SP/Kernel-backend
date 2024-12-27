package ru.lcp.kernel.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.lcp.kernel.dtos.ChatRequest;
import ru.lcp.kernel.dtos.ChatResponse;
import ru.lcp.kernel.entities.Message;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.NotificationPatterns;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.ChatNotFound;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.MessageRepository;
import ru.lcp.kernel.utils.MessageCryptographer;
import ru.lcp.kernel.utils.UserUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final UserUtils userUtils;
    private final MessageCryptographer messageCryptographer;


    @Transactional
    public ChatResponse saveMessage(UUID chatId, ChatRequest chatRequest) {
        String encryptedContent = messageCryptographer.encrypt(chatRequest.getContent());

        User sender;
        Chat chat;

        try {
            sender = userUtils.getByToken(chatRequest.getToken());
            chat = chatService.getChatById(chatId);
        } catch (UserNotFound | ChatNotFound e) {
            throw new RuntimeException(e);
        }

        Message message = new Message();

        message.setSender(sender);
        message.setContent(encryptedContent);
        message.setChat(chat);

        message = messageRepository.save(message);

        chat.getParticipants().forEach(chatParticipant -> {
            if (chatParticipant.getUser() != sender) {
                notificationService.sendNotification(sender, NotificationPatterns.NEW_MESSAGE, chatParticipant.getUser());
            }
        });

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setChatId(chatId);
        chatResponse.setSender(sender.getUsername());
        String timestamp = message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        chatResponse.setTimestamp(timestamp);
        chatResponse.setContent(messageCryptographer.decrypt(encryptedContent));
        chatResponse.setMessageId(message.getId());

        return chatResponse;
    }

    public List<ChatResponse> getMessages(UUID chatId) {
        Chat chat;

        try {
            chat = chatService.getChatById(chatId);
        } catch (ChatNotFound e) {
            throw new RuntimeException(e);
        }

        List<Message> messages = messageRepository.findByChatOrderByCreatedAt(chat);
        List<ChatResponse> chatResponses = new ArrayList<>();

        for (Message message : messages) {
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(message.getChat().getId());
            chatResponse.setSender(message.getSender().getUsername());
            chatResponse.setTimestamp(message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            chatResponse.setContent(messageCryptographer.decrypt(message.getContent()));
            chatResponse.setMessageId(message.getId());
            chatResponses.add(chatResponse);
        }

        return chatResponses;
    }

    @Transactional
    public ResponseEntity<?> deleteMessage(String token, UUID chatId, UUID messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);

        if (messageOptional.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "Message not found"), HttpStatus.NOT_FOUND);
        }

        Message message = messageOptional.get();

        try {
            User user = userUtils.getByToken(token);
            if (message.getSender().getId() == user.getId()) {
                messageRepository.deleteById(messageId);
                simpMessagingTemplate.convertAndSend("/topic/chat/history/" + chatId, getMessages(chatId));
                return ResponseEntity.ok("message deleted successfully");
            } else {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "Not enough rights"), HttpStatus.NOT_FOUND);
            }
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}