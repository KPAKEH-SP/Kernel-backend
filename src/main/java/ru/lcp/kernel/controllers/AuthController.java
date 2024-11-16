package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lcp.kernel.dtos.JwtRequest;
import ru.lcp.kernel.dtos.RegistrationUserDto;
import ru.lcp.kernel.dtos.UserInfoRequest;
import ru.lcp.kernel.services.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        return authService.createAuthToken(authRequest);
    }

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        return authService.createNewUser(registrationUserDto);
    }

    @PostMapping("/user-info")
    public ResponseEntity<?> userInfo(@RequestBody UserInfoRequest userInfoRequest) {
        return authService.userInfo(userInfoRequest);
    }
}
