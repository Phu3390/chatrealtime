package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.FriendRequestAction;
import com.example.chatrealtime.dto.request.SendFriendRequest;
import com.example.chatrealtime.entity.Friend;
import com.example.chatrealtime.entity.FriendRequest;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.FriendRequestStatus;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.repository.FriendRepository;
import com.example.chatrealtime.repository.FriendRequestRepository;
import com.example.chatrealtime.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FriendService {
    final FriendRepository repository;
    final FriendRequestRepository friendRequestRepository;

    final UserRepository userRepository;

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
        FriendRequest friendRequest = friendRequestRepository
                .findBySenderAndReceiver(sender, receiver)
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
                return;
            }
        }
        friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        friendRequestRepository.save(friendRequest);
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
                .findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXITS));

        FriendRequestStatus currentStatus = friendRequest.getStatus();
        FriendRequestStatus newStatus = request.getStatus();

        if (newStatus != FriendRequestStatus.ACCEPTED &&
                newStatus != FriendRequestStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (currentStatus == FriendRequestStatus.PENDING &&
                newStatus == FriendRequestStatus.ACCEPTED) {

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

            return;
        }

        if (currentStatus == FriendRequestStatus.PENDING &&
                newStatus == FriendRequestStatus.REJECTED) {

            friendRequest.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepository.save(friendRequest);
            return;
        }
        if (currentStatus == FriendRequestStatus.ACCEPTED &&
                newStatus == FriendRequestStatus.REJECTED) {
            repository.deleteByUserIdAndFriendId(sender.getId(), receiver.getId());
            repository.deleteByUserIdAndFriendId(receiver.getId(), sender.getId());
            friendRequest.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepository.save(friendRequest);
            return;
        }
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }

}
