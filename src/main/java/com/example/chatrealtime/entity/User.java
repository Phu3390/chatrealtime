package com.example.chatrealtime.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.UserStatus;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String avatar;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}