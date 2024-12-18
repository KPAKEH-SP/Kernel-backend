package ru.lcp.kernel.services;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.utils.JwtTokenUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> createAuthToken(JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.UNAUTHORIZED.value(), "Incorrect login or password"), HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    public ResponseEntity<?> createNewUser(RegistrationUserDto registrationUserDto) {
        if (userService.findByUsername(registrationUserDto.getUsername()).isPresent()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "A user with this login already exists"), HttpStatus.CONFLICT);
        }

        if (userService.findByEmail(registrationUserDto.getEmail()).isPresent()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "A user with this email already exists"), HttpStatus.CONFLICT);
        }

        UserDetails userDetails = userService.createNewUser(registrationUserDto);

        String newUserToken = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok(newUserToken);
    }

    public ResponseEntity<?> userInfo(String token) {
        try {
            String username = jwtTokenUtils.getUsername(token);
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.UNAUTHORIZED.value(), "User not found"), HttpStatus.NOT_FOUND);
            }

            User user = userOptional.get();

            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setUsername(user.getUsername());
            userInfoResponse.setEmail(user.getEmail());

            return ResponseEntity.ok(userInfoResponse);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "Expired token"), HttpStatus.UNAUTHORIZED);
        }

    }
}
