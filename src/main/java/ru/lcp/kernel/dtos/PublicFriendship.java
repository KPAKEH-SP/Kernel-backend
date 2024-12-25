package ru.lcp.kernel.dtos;

import lombok.Data;
import ru.lcp.kernel.entities.Friendship;

@Data
public class PublicFriendship {
    private PublicUser user;
    private String status;
    private PublicUser pendingFrom;

    public PublicFriendship(Friendship friendship) {
        this.user = new PublicUser(friendship.getFriend());
        this.status = friendship.getStatus();
        this.pendingFrom = new PublicUser(friendship.getUser());
    }
}
