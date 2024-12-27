package ru.lcp.kernel.entities;

import jakarta.persistence.*;
import lombok.Data;
import ru.lcp.kernel.enums.ChatRoles;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "chat_participants")
public class ChatParticipant {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ChatRoles role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
}
