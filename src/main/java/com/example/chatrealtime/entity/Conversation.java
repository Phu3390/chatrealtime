package com.example.chatrealtime.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.ConversationType;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ConversationType type;

    @Column(length = 100)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}