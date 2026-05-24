package com.example.chatrealtime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.UpdateProfileRequest;
import com.example.chatrealtime.dto.response.FriendRequestResponse;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.entity.Friend;
import com.example.chatrealtime.entity.FriendRequest;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.FriendRequestStatus;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.mapper.FriendRequestMapper;
import com.example.chatrealtime.mapper.UserMapper;
import com.example.chatrealtime.repository.FriendRepository;
import com.example.chatrealtime.repository.FriendRequestRepository;
import com.example.chatrealtime.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserService {
    final UserRepository repository;
    final UserMapper userMapper;
    final FriendRepository friendRepository;
    final FriendRequestRepository friendRequestRepository;
    final FriendRequestMapper friendRequestMapper;

    public List<UserResponse> searchUsers(String keyword) {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        // Pending
        List<FriendRequest> requests = friendRequestRepository
                .findBySenderIdAndStatus(
                        currentUserId,
                        FriendRequestStatus.PENDING);

        // Accepted
        List<Friend> friends = friendRepository
                .findByUserId(currentUserId);

        List<UUID> excludedIds = new ArrayList<>();

        excludedIds.add(currentUserId);

        for (Friend friend : friends) {
            excludedIds.add(friend.getFriendId());
        }
        for (FriendRequest request : requests) {
            excludedIds.add(
                    request.getReceiver().getId());
        }
        List<User> users = repository.findTop10ByFullNameContainingIgnoreCaseAndIdNotIn(keyword.trim(), excludedIds);
        return userMapper.toResponseList(users);
    }

    public List<FriendRequestResponse> getFriendStatus(FriendRequestStatus status) {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));

        if (status == FriendRequestStatus.ACCEPTED) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        List<FriendRequest> requests = friendRequestRepository.findBySenderIdAndStatus(currentUserId, status);
        return friendRequestMapper.toListResponse(requests);
    }

    public List<FriendRequestResponse> getListFriendInvitation() {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(currentUserId,
                FriendRequestStatus.PENDING);
        return friendRequestMapper.toListResponse(requests);
    }

    public int countFriendInvitation() {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));
        return friendRequestRepository.countByReceiverIdAndStatus(currentUserId, FriendRequestStatus.PENDING);
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        UUID currentUserId = UUID.fromString(
                (String) ((org.springframework.security.oauth2.jwt.Jwt) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())
                        .getClaims().get("userId"));

        User user = repository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
        if(repository.existsByEmailAndIdNot(request.getEmail(), currentUserId)) {
            throw new AppException(ErrorCode.EMAIL_EXITS);
        }
        userMapper.updateUserEntity(user, request);
        repository.save(user);
        return userMapper.toResponse(user);
    }
}
