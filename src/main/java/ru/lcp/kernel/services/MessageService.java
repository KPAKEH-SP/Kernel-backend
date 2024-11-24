package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lcp.kernel.dtos.ChatRequest;
import ru.lcp.kernel.dtos.ChatResponse;
import ru.lcp.kernel.entities.ChatMessage;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.repositories.MessageRepository;
import ru.lcp.kernel.repositories.UserRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;
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

    // Метод шифрования
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

    // Метод расшифровки
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

    public ChatResponse saveMessage(Long chatId, ChatRequest chatRequest) {
        String encryptedContent = encrypt(chatRequest.getContent());

        String senderUsername = jwtTokenUtils.getUsername(chatRequest.getSender());
        User sender = userRepository.findByUsername(senderUsername).get();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatId(chatId);
        chatMessage.setContent(encryptedContent);
        chatMessage.setSender(sender);

        messageRepository.save(chatMessage);

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setChatId(chatId);
        chatResponse.setSender(sender.getUsername());
        String timestamp = chatMessage.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        chatResponse.setTimestamp(timestamp);
        chatResponse.setContent(decrypt(encryptedContent));

        return chatResponse;
    }

    public List<ChatResponse> getMessages(Long chatId) {
        List<ChatMessage> messages = messageRepository.findByChatIdOrderByTimestamp(chatId);
        List<ChatResponse> chatResponses = new ArrayList<>();

        for (ChatMessage message : messages) {
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setChatId(message.getChatId());
            chatResponse.setSender(message.getSender().getUsername());
            chatResponse.setTimestamp(message.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            chatResponse.setContent(decrypt(message.getContent()));
            chatResponses.add(chatResponse);
        }

        return chatResponses;
    }
}