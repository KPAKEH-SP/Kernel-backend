package ru.lcp.kernel.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "friendships")
@Data
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    @Column(name = "status")
    private String status;
}
