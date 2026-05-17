package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.AddParticipantRequest;
import com.example.chatrealtime.dto.request.CreateConversationRequest;
import com.example.chatrealtime.dto.response.ConversationResponse;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.dto.response.ParticipantResponse;
import com.example.chatrealtime.entity.Conversation;
import com.example.chatrealtime.entity.ConversationParticipant;
import com.example.chatrealtime.entity.Message;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.ConversationParticipantRole;
import com.example.chatrealtime.enums.ConversationType;
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
public class ConversationService {
        final ConversationRepository conversationRepository;
        final ConversationParticipantRepository conversationParticipantRepository;
        final UserRepository userRepository;
        final MessageRepository messageRepository;

        @Transactional
        public ConversationResponse createConversation(CreateConversationRequest request) {

                UUID currentUserId = UUID.fromString(
                                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                                .getContext().getAuthentication().getPrincipal())
                                                .getClaims().get("userId"));

                User currentUser = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

                if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                }

                Conversation conversation;

                if (request.getType() == ConversationType.PRIVATE) {

                        if (request.getParticipantIds().size() != 1) {
                                throw new AppException(ErrorCode.INVALID_REQUEST);
                        }

                        UUID otherUserId = request.getParticipantIds().get(0);

                        if (otherUserId.equals(currentUserId)) {
                                throw new AppException(ErrorCode.INVALID_REQUEST);
                        }

                        User otherUser = userRepository.findById(otherUserId)
                                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

                        conversation = Conversation.builder()
                                        .type(ConversationType.PRIVATE)
                                        .name(null)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        conversationRepository.save(conversation);

                        conversationParticipantRepository.save(
                                        ConversationParticipant.builder()
                                                        .conversationId(conversation.getId())
                                                        .userId(currentUser.getId())
                                                        .role(ConversationParticipantRole.MEMBER)
                                                        .isMuted(false)
                                                        .joinedAt(LocalDateTime.now())
                                                        .build());

                        conversationParticipantRepository.save(
                                        ConversationParticipant.builder()
                                                        .conversationId(conversation.getId())
                                                        .userId(otherUser.getId())
                                                        .role(ConversationParticipantRole.MEMBER)
                                                        .isMuted(false)
                                                        .joinedAt(LocalDateTime.now())
                                                        .build());
                }

                // =========================
                // GROUP CHAT
                // =========================
                else if (request.getType() == ConversationType.GROUP) {

                        conversation = Conversation.builder()
                                        .type(ConversationType.GROUP)
                                        .name(request.getName())
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        conversationRepository.save(conversation);

                        conversationParticipantRepository.save(
                                        ConversationParticipant.builder()
                                                        .conversationId(conversation.getId())
                                                        .userId(currentUserId)
                                                        .role(ConversationParticipantRole.ADMIN)
                                                        .joinedAt(LocalDateTime.now())
                                                        .isMuted(false)
                                                        .build());

                        for (UUID participantId : request.getParticipantIds()) {

                                if (participantId.equals(currentUserId)) {
                                        continue;
                                }

                                User participant = userRepository.findById(participantId)
                                                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

                                if (!conversationParticipantRepository.existsByConversationIdAndUserId(
                                                conversation.getId(),
                                                participant.getId())) {

                                        conversationParticipantRepository.save(
                                                        ConversationParticipant.builder()
                                                                        .conversationId(conversation.getId())
                                                                        .userId(participant.getId())
                                                                        .role(ConversationParticipantRole.MEMBER)
                                                                        .joinedAt(LocalDateTime.now())
                                                                        .isMuted(false)
                                                                        .build());
                                }
                        }
                } else {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                }

                // =========================
                // BUILD RESPONSE
                // =========================
                List<ConversationParticipant> participants = conversationParticipantRepository
                                .findByConversationId(conversation.getId());

                List<ParticipantResponse> participantResponses = participants.stream().map(participant -> {

                        User user = userRepository.findById(participant.getUserId())
                                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

                        return ParticipantResponse.builder()
                                        .userId(user.getId())
                                        .fullName(user.getFullName())
                                        .avatar(user.getAvatar())
                                        .role(participant.getRole())
                                        .isMuted(participant.getIsMuted())
                                        .joinedAt(participant.getJoinedAt())
                                        .build();

                }).toList();

                return ConversationResponse.builder()
                                .conversationId(conversation.getId())
                                .type(conversation.getType())
                                .name(conversation.getName())
                                .createdAt(conversation.getCreatedAt())
                                .participants(participantResponses)
                                .build();
        }

        public List<ConversationSummaryResponse> getMyConversations() {

                UUID currentUserId = UUID.fromString(
                                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                                .getContext().getAuthentication().getPrincipal())
                                                .getClaims().get("userId"));

                List<ConversationParticipant> myParticipations = conversationParticipantRepository
                                .findByUserId(currentUserId);

                List<ConversationSummaryResponse> responses = myParticipations.stream()
                                .map(participant -> {

                                        Conversation conversation = conversationRepository
                                                        .findById(participant.getConversationId())
                                                        .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));

                                        Optional<Message> optionalLastMessage = messageRepository
                                                        .findTopByConversation_IdOrderByCreatedAtDesc(
                                                                        conversation.getId());
                                        if (optionalLastMessage.isEmpty()) {
                                                return ConversationSummaryResponse.builder()
                                                                .conversationId(conversation.getId())
                                                                .type(conversation.getType())
                                                                .name(conversation.getName())
                                                                .unreadCount(0)
                                                                .build();
                                        }
                                        Message lastMessage = optionalLastMessage.get();
                                        User sender = userRepository.findById(lastMessage.getSender().getId())
                                                        .orElseThrow(() -> new AppException(
                                                                        ErrorCode.ACCOUNT_NOT_EXITS));

                                        return ConversationSummaryResponse.builder()
                                                        .conversationId(conversation.getId())
                                                        .type(conversation.getType())
                                                        .name(conversation.getName())
                                                        .lastMessage(lastMessage.getContent())
                                                        .lastMessageType(lastMessage.getMessageType())
                                                        .lastSenderId(sender.getId())
                                                        .lastSenderName(sender.getFullName())
                                                        .lastMessageAt(lastMessage.getCreatedAt())
                                                        .unreadCount(
                                                                        messageRepository.countUnreadMessages(
                                                                                        conversation.getId(),
                                                                                        participant.getUserId(),
                                                                                        participant.getJoinedAt()))
                                                        .build();
                                })
                                .collect(java.util.stream.Collectors.toList());

                // Sort mới nhất lên đầu
                responses.sort((a, b) -> {

                        if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) {
                                return 0;
                        }

                        if (a.getLastMessageAt() == null) {
                                return 1;
                        }

                        if (b.getLastMessageAt() == null) {
                                return -1;
                        }

                        return b.getLastMessageAt().compareTo(a.getLastMessageAt());
                });

                return responses;
        }

        @Transactional
        public void addParticipant(AddParticipantRequest request) {

                UUID currentUserId = UUID
                                .fromString((String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                                .getContext()
                                                .getAuthentication().getPrincipal())
                                                .getClaims().get("userId"));

                Conversation conversation = conversationRepository.findById(request.getConversationId())
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));

                if (conversation.getType() != ConversationType.GROUP) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                }

                ConversationParticipant currentParticipant = conversationParticipantRepository
                                .findByConversationIdAndUserId(conversation.getId(), currentUserId)
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

                if (currentParticipant.getRole() != ConversationParticipantRole.ADMIN) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                User newUser = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

                if (conversationParticipantRepository.existsByConversationIdAndUserId(
                                conversation.getId(),
                                newUser.getId())) {
                        throw new AppException(ErrorCode.ALREADY_EXISTS);
                }

                conversationParticipantRepository.save(ConversationParticipant.builder()
                                .conversationId(conversation.getId())
                                .userId(newUser.getId())
                                .role(request.getRole() == null
                                                ? ConversationParticipantRole.MEMBER
                                                : request.getRole())
                                .joinedAt(LocalDateTime.now())
                                .isMuted(false)
                                .build());
        }
}
