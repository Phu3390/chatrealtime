package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.AddParticipantRequest;
import com.example.chatrealtime.dto.request.CreateConversationRequest;
import com.example.chatrealtime.dto.response.ConversationResponse;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.dto.response.ParticipantResponse;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.entity.Conversation;
import com.example.chatrealtime.entity.ConversationParticipant;
import com.example.chatrealtime.entity.Message;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.ConversationParticipantRole;
import com.example.chatrealtime.enums.ConversationType;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.mapper.UserMapper;
import com.example.chatrealtime.repository.ConversationParticipantRepository;
import com.example.chatrealtime.repository.ConversationRepository;
import com.example.chatrealtime.repository.FriendRepository;
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
        final UserMapper userMapper;
        final FriendRepository friendRepository;

        public void createPrivateConversationIfNotExists(UUID senderId, UUID userId) {
                boolean isFriend = friendRepository.existsByUserIdAndFriendId(senderId, userId);

                if (!isFriend) {
                        return;
                }
                boolean hasConversation = hasPrivateConversation(senderId, userId);
                if (hasConversation) {
                        return;
                }
                CreateConversationRequest request = new CreateConversationRequest();
                        request.setType(ConversationType.PRIVATE);
                        request.setParticipantIds(List.of(userId));
                        request.setName(null);
                        createConversation(request);
        }

        private boolean hasPrivateConversation(UUID user1, UUID user2) {
                List<UUID> user1ConversationIds = conversationParticipantRepository.findByUserId(user1)
                        .stream().map(ConversationParticipant::getConversationId).toList();

                List<UUID> user2ConversationIds = conversationParticipantRepository.findByUserId(user2)
                        .stream().map(ConversationParticipant::getConversationId).toList();

                return user1ConversationIds.stream()
                        .filter(user2ConversationIds::contains)
                        .map(conversationId -> conversationRepository.findById(conversationId).orElse(null))
                        .filter(Objects::nonNull)
                        .anyMatch(conversation -> conversation.getType() == ConversationType.PRIVATE);
        }

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
                UUID currentUserId = UUID.fromString((String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                                .getContext().getAuthentication().getPrincipal())
                                .getClaims().get("userId"));
                List<ConversationParticipant> myParticipations = conversationParticipantRepository.findByUserId(currentUserId);
                return myParticipations.stream()
                        .map(participant -> buildConversationSummary(participant, currentUserId))
                        .filter(Objects::nonNull)
                        .sorted(this::sortConversation)
                        .toList();
        }
        private ConversationSummaryResponse buildConversationSummary(ConversationParticipant participant, UUID currentUserId) {

                Conversation conversation = conversationRepository
                        .findById(participant.getConversationId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));
                UserResponse targetUser = null;

                if (conversation.getType() == ConversationType.PRIVATE) {
                        ConversationParticipant targetParticipant = getTargetParticipant(conversation.getId(), currentUserId);
                        if (targetParticipant == null) {
                        return null;
                        }
                        boolean isFriend = friendRepository.existsByUserIdAndFriendId(currentUserId,targetParticipant.getUserId());
                        if (!isFriend) {
                                return null;
                        }
                        User target = userRepository.findById(targetParticipant.getUserId())
                                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
                        targetUser = userMapper.toResponse(target);
                }

                Optional<Message> optionalLastMessage =
                        messageRepository.findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId());

                if (optionalLastMessage.isEmpty()) {
                        return ConversationSummaryResponse.builder()
                                .conversationId(conversation.getId())
                                .type(conversation.getType())
                                .name(conversation.getName())
                                .targetUser(targetUser)
                                .unreadCount(0)
                                .build();
                }
                Message lastMessage = optionalLastMessage.get();

                return ConversationSummaryResponse.builder()
                        .conversationId(conversation.getId())
                        .type(conversation.getType())
                        .name(conversation.getName())
                        .targetUser(targetUser)
                        .lastMessage(lastMessage.getContent())
                        .lastMessageType(lastMessage.getMessageType())
                        .lastSenderId(lastMessage.getSender().getId())
                        .lastSenderName(lastMessage.getSender().getFullName())
                        .lastMessageAt(lastMessage.getCreatedAt())
                        .unreadCount(messageRepository.countUnreadMessages(
                                        conversation.getId(),
                                        participant.getUserId(),
                                        participant.getJoinedAt())).build();
        
        }

        private ConversationParticipant getTargetParticipant(UUID conversationId, UUID currentUserId) {
                return conversationParticipantRepository
                        .findByConversationId(conversationId)
                        .stream()
                        .filter(participant -> !participant.getUserId().equals(currentUserId))
                        .findFirst()
                        .orElse(null);
                }
        private int sortConversation(ConversationSummaryResponse a, ConversationSummaryResponse b) {
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
