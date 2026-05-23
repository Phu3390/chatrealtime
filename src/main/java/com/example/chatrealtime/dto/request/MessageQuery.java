package com.example.chatrealtime.dto.request;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageQuery {
    private UUID conversationId;
    private LocalDateTime before;
    private Integer size;
}
