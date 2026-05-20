package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.UserStatus;

import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String avatar;
}