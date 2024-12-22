package ru.lcp.kernel.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.lcp.kernel.dtos.ChatRequest;
import ru.lcp.kernel.dtos.ChatResponse;
import ru.lcp.kernel.entities.ChatMessage;
import ru.lcp.kernel.entities.PersonalChat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.NotificationPatterns;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.MessageRepository;
import ru.lcp.kernel.repositories.UserRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;
import ru.lcp.kernel.utils.UserUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PersonalChatService chatService;
    private final NotificationService notificationService;
    private final UserUtils userUtils;
    @Value("${jwt.secret-messages}")
    private String secret;

    private SecretKeySpec generateKey() {
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key); // Преобразуем ключ в 256 бит
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации ключа", e);
        }
    }

    public String encrypt(String message) {
        try {
            SecretKeySpec keySpec = generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования", e);
        }
    }

    public String decrypt(String encryptedMessage) {
        try {
            SecretKeySpec keySpec = generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
            return new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расшифровки", e);
        }
    }

    @Transactional
    public ChatResponse saveMessage(UUID chatId, ChatRequest chatRequest) {
        String encryptedContent = encrypt(chatRequest.getContent());

        String senderUsername = jwtTokenUtils.getUsername(chatRequest.getSender());
        User sender = userRepository.findByUsername(senderUsername).get();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setContent(encryptedContent);
        chatMessage.setSender(sender);

        ChatMessage message = messageRepository.save(chatMessage);

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setChatId(chatId);
        chatResponse.setSender(sender.getUsername());
        String timestamp = chatMessage.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        chatResponse.setTimestamp(timestamp);
        chatResponse.setContent(decrypt(encryptedContent));
        chatResponse.setMessageId(message.getId());

        PersonalChat personalChat = chatService.getPersonalChat(chatId);

        if (personalChat.getFirstUser() != sender) {
            notificationService.sendNotification(sender, NotificationPatterns.NEW_MESSAGE, personalChat.getFirstUser());
        } else {
            notificationService.sendNotification(sender, NotificationPatterns.NEW_MESSAGE, personalChat.getSecondUser());
        }

        return chatResponse;
    }

    public List<ChatResponse> getMessages(UUID chatId) {
        List<ChatMessage> messages = messageRepository.findByChatIdOrderByTimestamp(chatId);
        List<ChatResponse> chatResponses = new ArrayList<>();

        for (ChatMessage message : messages) {
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(message.getChatId());
            chatResponse.setSender(message.getSender().getUsername());
            chatResponse.setTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            chatResponse.setContent(decrypt(message.getContent()));
            chatResponse.setMessageId(message.getId());
            chatResponses.add(chatResponse);
        }

        return chatResponses;
    }

    @Transactional
    public ResponseEntity<?> deleteMessage(String token, UUID chatId, UUID messageId) {
        Optional<ChatMessage> messageOptional =  messageRepository.findById(messageId);

        if (messageOptional.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "Message not found"), HttpStatus.NOT_FOUND);
        }

        ChatMessage message = messageOptional.get();

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