package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.utils.UserUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AvatarService {
    private final UserUtils userUtils;
    @Value("${uploads.avatar-dir}")
    private String avatarDir;

    public ResponseEntity<?> getAvatar(String username) {
        User user;
        try {
            user = userUtils.getByUsername(username);
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        String fileName = user.getId().toString() + ".gif";
        Path filePath = Paths.get(avatarDir + fileName);

        Resource resource = new FileSystemResource(filePath);

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_GIF)
                    .body(resource);
        } else {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "Avatar not found"), HttpStatus.NOT_FOUND);
        }

    }

    @Transactional
    public ResponseEntity<?> uploadAvatar(String userToken, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                System.out.println("File is empty");
                return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "File is empty"), HttpStatus.NO_CONTENT);
            }

            User user = userUtils.getByToken(userToken);

            String mimeType = file.getContentType();
            if (mimeType == null || !mimeType.startsWith("image/")) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "File is not image"), HttpStatus.CONFLICT);
            }

            String fileName = user.getId().toString() + ".gif";
            Path filePath = Paths.get(avatarDir + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            return ResponseEntity.ok(HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "Error uploading file"), HttpStatus.NOT_FOUND);
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}
