package ru.lcp.kernel.dtos;

import lombok.Data;
import ru.lcp.kernel.entities.User;

@Data
public class PublicUser {
    private String username;

    public PublicUser(User user) {
        this.username = user.getUsername();
    }
}
