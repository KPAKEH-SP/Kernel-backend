package ru.lcp.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserUtils {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserRepository userRepository;

    public User getByToken(String token) throws UserNotFound {
        String username = jwtTokenUtils.getUsername(token);
        return getByUsername(username);
    }

    public User getByUsername(String username) throws UserNotFound {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new UserNotFound();
        }

        return userOpt.get();
    }
}
