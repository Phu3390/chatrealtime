package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.mapstruct.control.MappingControl.Use;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.SendMessageRequest;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.dto.response.MessageResponse;
import com.example.chatrealtime.entity.Conversation;
import com.example.chatrealtime.entity.ConversationParticipant;
import com.example.chatrealtime.entity.Message;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.MessageType;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.repository.ConversationParticipantRepository;
import com.example.chatrealtime.repository.ConversationRepository;
import com.example.chatrealtime.repository.MessageRepository;
import com.example.chatrealtime.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MessageService {
    final MessageRepository messageRepository;
    final UserRepository userRepository;
    final ConversationRepository conversationRepository;
    final ConversationParticipantRepository conversationParticipantRepository;
    final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));

        ConversationParticipant participant = conversationParticipantRepository
                .findByConversationIdAndUserId(conversation.getId(), currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

        if (Boolean.TRUE.equals(participant.getIsMuted())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        MessageType messageType = request.getMessageType() == null
                ? MessageType.TEXT
                : request.getMessageType();

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent().trim())
                .messageType(messageType)
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        MessageResponse messageResponse = MessageResponse.builder()
                .messageId(message.getId())
                .conversation(conversation)
                .sender(sender)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversation.getId(),
                messageResponse);
                
        List<ConversationParticipant> participants = conversationParticipantRepository.findByConversationId(conversation.getId());

        for (ConversationParticipant participant1 : participants) {

            int unreadCount = participant1.getUserId().equals(sender.getId())
                    ? 0
                    : messageRepository.countUnreadMessages(
                            conversation.getId(),
                            participant1.getUserId(),
                            participant1.getJoinedAt());

            ConversationSummaryResponse summary = ConversationSummaryResponse.builder()
                    .conversationId(conversation.getId())
                    .type(conversation.getType())
                    .name(conversation.getName())
                    .lastMessage(message.getContent())
                    .lastMessageType(message.getMessageType())
                    .lastSenderId(sender.getId())
                    .lastSenderName(sender.getFullName())
                    .lastMessageAt(message.getCreatedAt())
                    .unreadCount(unreadCount)
                    .build();

            messagingTemplate.convertAndSendToUser(
                    participant1.getUserId().toString(),
                    "/queue/conversations",
                    summary);
        }

        return messageResponse;

    }
}
