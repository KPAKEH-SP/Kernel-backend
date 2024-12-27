package ru.lcp.kernel.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class MessageCryptographer {
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
}
