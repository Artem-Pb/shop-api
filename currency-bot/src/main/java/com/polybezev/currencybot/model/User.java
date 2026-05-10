package com.polybezev.currencybot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long chatId;

    @Column
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
