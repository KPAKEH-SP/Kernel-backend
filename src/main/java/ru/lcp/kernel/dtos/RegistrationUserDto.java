package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class RegistrationUserDto {
    private String username;
    private String password;
    private String email;
}
