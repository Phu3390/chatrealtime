package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.MessageQuery;
import com.example.chatrealtime.dto.request.SendMessageRequest;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.dto.response.MessageResponse;
import com.example.chatrealtime.dto.response.PageResponse;
import com.example.chatrealtime.entity.Conversation;
import com.example.chatrealtime.entity.ConversationParticipant;
import com.example.chatrealtime.entity.Message;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.MessageType;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.mapper.MessageMapper;
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
        final MessageMapper messageMapper;

        @Transactional
        public MessageResponse sendMessage(SendMessageRequest request) {
                UUID currentUserId = UUID.fromString(
                                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                                .getContext()
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

                MessageResponse messageResponse = messageMapper.toResponse(message);

                messagingTemplate.convertAndSend(
                                "/topic/conversation/" + conversation.getId(),
                                messageResponse);

                List<ConversationParticipant> participants = conversationParticipantRepository
                                .findByConversationId(conversation.getId());

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

        public PageResponse<MessageResponse> getMessages(MessageQuery query) {
                 UUID currentUserId = UUID.fromString(
                                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                                .getContext()
                                                .getAuthentication().getPrincipal())
                                                .getClaims().get("userId"));
                if(!conversationParticipantRepository.existsByConversationIdAndUserId(
                                query.getConversationId(), currentUserId)) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }
                int size = resolveSize(query.getSize());
                Pageable pageable = PageRequest.of(0, size);
                List<Message> messages;
                if (query.getBefore() == null) {
                        messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                                        query.getConversationId(),
                                        pageable);
                } else {
                        messages = messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                                        query.getConversationId(),
                                        query.getBefore(),
                                        pageable);
                }
                boolean hasMore = messages.size() == size;
                Collections.reverse(messages);
                LocalDateTime nextCursor = hasMore
                                ? messages.get(0).getCreatedAt()
                                : null;
                return PageResponse.<MessageResponse>builder()
                                .content(messages.stream().map(messageMapper::toResponse).toList())
                                .size(size)
                                .returned(messages.size())
                                .cursor(query.getBefore())
                                .nextCursor(nextCursor)
                                .hasMore(hasMore)
                                .build();
        }

        private int resolveSize(Integer size) {
                if (size == null)
                        return 20;
                return Math.min(size, 50);
        }
}
