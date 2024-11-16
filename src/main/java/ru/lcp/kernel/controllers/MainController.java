package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.JwtRequest;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {
    @GetMapping("/account")
    public String account() {
        return "Account OK";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Admin OK";
    }

    @GetMapping("/user")
    public String user(Principal principal) {
        return principal.getName();
    }
}
