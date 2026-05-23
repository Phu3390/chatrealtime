package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.FriendRequestAction;
import com.example.chatrealtime.dto.request.SendFriendRequest;
import com.example.chatrealtime.dto.response.FriendRealtimeResponse;
import com.example.chatrealtime.entity.Friend;
import com.example.chatrealtime.entity.FriendRequest;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.FriendRequestStatus;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.mapper.UserMapper;
import com.example.chatrealtime.repository.FriendRepository;
import com.example.chatrealtime.repository.FriendRequestRepository;
import com.example.chatrealtime.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FriendService {
    final FriendRepository repository;
    final FriendRequestRepository friendRequestRepository;
    final ConversationService conversationService;

    final UserRepository userRepository;
    final UserMapper userMapper;
    final SimpMessagingTemplate messagingTemplate;

    public void sendFriendRequest(SendFriendRequest request) {
        UUID senderId = UUID.fromString((String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal())
                .getClaims().get("userId"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        if (sender.getId().equals(receiver.getId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)
                || friendRequestRepository.existsByReceiverAndSenderAndStatus(sender, receiver,
                        FriendRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
        }

        FriendRequest friendRequest = friendRequestRepository
                .findBySenderAndReceiverOrSenderAndReceiver(
                        sender,
                        receiver,
                        receiver,
                        sender)
                .orElse(null);
        if (friendRequest != null) {
            if (friendRequest.getStatus() == FriendRequestStatus.PENDING) {
                throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
            }
            if (friendRequest.getStatus() == FriendRequestStatus.ACCEPTED) {
                throw new AppException(ErrorCode.ALREADY_FRIENDS);
            }
            if (friendRequest.getStatus() == FriendRequestStatus.REJECTED) {
                friendRequest.setStatus(FriendRequestStatus.PENDING);
                friendRequest.setCreatedAt(LocalDateTime.now());
                friendRequestRepository.save(friendRequest);

                FriendRealtimeResponse response = FriendRealtimeResponse.builder()
                        .requestId(friendRequest.getId())
                        .sender(userMapper.toResponse(friendRequest.getSender()))
                        .build();
                messagingTemplate.convertAndSend("/topic/friend-request/" + receiver.getId(), response);
                return;
            }
        }
        friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        friendRequest = friendRequestRepository.save(friendRequest);

        FriendRealtimeResponse response = FriendRealtimeResponse.builder()
                .requestId(friendRequest.getId())
                .sender(userMapper.toResponse(friendRequest.getSender()))
                .build();
        messagingTemplate.convertAndSend("/topic/friend-request/" + receiver.getId(), response);
    }

    @Transactional
    public void actionFriendRequest(FriendRequestAction request) {
        UUID receiverId = UUID.fromString((String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal())
                .getClaims().get("userId"));

        User sender = userRepository.findById(request.getRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        FriendRequest friendRequest = friendRequestRepository
                .findBySenderAndReceiverOrSenderAndReceiver(
                        sender,
                        receiver,
                        receiver,
                        sender)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));

        FriendRequestStatus currentStatus = friendRequest.getStatus();
        FriendRequestStatus newStatus = request.getStatus();

        if (newStatus != FriendRequestStatus.ACCEPTED && newStatus != FriendRequestStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if ((currentStatus == FriendRequestStatus.PENDING || currentStatus == FriendRequestStatus.REJECTED)
                && newStatus == FriendRequestStatus.ACCEPTED) {
            friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(friendRequest);

            if (!repository.existsByUserIdAndFriendId(sender.getId(), receiver.getId())) {
                repository.save(Friend.builder()
                        .userId(sender.getId())
                        .friendId(receiver.getId())
                        .createdAt(LocalDateTime.now())
                        .build());
            }

            if (!repository.existsByUserIdAndFriendId(receiver.getId(), sender.getId())) {
                repository.save(Friend.builder()
                        .userId(receiver.getId())
                        .friendId(sender.getId())
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            conversationService.createPrivateConversationIfNotExists(receiver.getId(), sender.getId());
            return;
        }
        if (currentStatus == FriendRequestStatus.PENDING && newStatus == FriendRequestStatus.REJECTED) {
            friendRequestRepository.delete(friendRequest);
            return;
        }
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }

    @Transactional
    public void removeFriend(UUID friendId) {
        UUID senderId = UUID.fromString((String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal())
                .getClaims().get("userId"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
        if (!repository.existsByUserIdAndFriendId(sender.getId(), friend.getId()) ||
                !repository.existsByUserIdAndFriendId(friend.getId(), sender.getId())) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }
        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, friend, FriendRequestStatus.PENDING) ||
                friendRequestRepository.existsByReceiverAndSenderAndStatus(sender, friend,
                        FriendRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, friend, FriendRequestStatus.REJECTED) ||
                friendRequestRepository.existsByReceiverAndSenderAndStatus(sender, friend,
                        FriendRequestStatus.REJECTED)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        repository.deleteByUserIdAndFriendId(senderId, friendId);
        repository.deleteByUserIdAndFriendId(friendId, senderId);
        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, friend, FriendRequestStatus.ACCEPTED)) {
                friendRequestRepository.deleteBySenderAndReceiver(sender, friend);
        }
        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(friend, sender, FriendRequestStatus.ACCEPTED)) {
            friendRequestRepository.deleteBySenderAndReceiver(friend, sender);
        }
    }

}
