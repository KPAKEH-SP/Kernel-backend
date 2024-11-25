package ru.lcp.kernel.utils;

import org.springframework.stereotype.Component;
import ru.lcp.kernel.dtos.UserPublicInfo;
import ru.lcp.kernel.entities.User;

@Component
public class PrivateUserConvertor {
    public UserPublicInfo convertUserPublicInfo(User user) {
        UserPublicInfo userPublicInfo = new UserPublicInfo();
        userPublicInfo.setUsername(user.getUsername());

        return userPublicInfo;
    }
}
