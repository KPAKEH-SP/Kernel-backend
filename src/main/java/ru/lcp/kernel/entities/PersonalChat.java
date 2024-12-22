package ru.lcp.kernel.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "personal_chats")
public class PersonalChat {
    @Id
    @Column(name = "id")
    private UUID chatId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_user_id", nullable = false)
    private User firstUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id", nullable = false)
    private User secondUser;
}
