package ru.lcp.kernel.dtos.webSocketV2;

import lombok.Data;
import ru.lcp.kernel.dtos.PublicFriendship;

import java.util.List;

@Data
public class FriendPayload {
    private List<PublicFriendship> friends;
}
