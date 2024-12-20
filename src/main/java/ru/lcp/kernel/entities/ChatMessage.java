package ru.lcp.kernel.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "messages")
public class ChatMessage {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "chat_id", nullable = false)
    private UUID chatId;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}