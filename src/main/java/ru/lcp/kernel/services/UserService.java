package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.lcp.kernel.dtos.RegistrationUserDto;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.repositories.UserRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    @Value("${uploads.avatar-dir}")
    private String avatarDir;

    public Optional<User> findByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList()));
    }

    @Transactional
    public UserDetails createNewUser(RegistrationUserDto registrationUserDto) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(registrationUserDto.getUsername());
        user.setEmail(registrationUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
        user.setRoles(List.of(roleService.getUserRole()));
        userRepository.save(user);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList()));
    }

    @Transactional
    public ResponseEntity<?> uploadAvatar(String userToken, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                System.out.println("File is empty");
                return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "File is empty"), HttpStatus.NO_CONTENT);
            }

            String username = jwtTokenUtils.getUsername(userToken);
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "User not found"), HttpStatus.NOT_FOUND);
            }

            User user = userOpt.get();

            String mimeType = file.getContentType();
            if (mimeType == null || !mimeType.startsWith("image/")) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "File is not image"), HttpStatus.CONFLICT);
            }

            String fileName = user.getId().toString() + ".gif";
            Path filePath = Paths.get(avatarDir + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            return getAvatar(user.getUsername());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
        }
    }

    public ResponseEntity<?> getAvatar(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.NOT_FOUND.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        String fileName = user.getId().toString() + ".gif";
        Path filePath = Paths.get(avatarDir + fileName);

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .body(resource);
    }
}
