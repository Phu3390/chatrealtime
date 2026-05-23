package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.FriendRequest;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.FriendRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

        List<FriendRequest> findBySender(User sender);

        Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

        boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

        boolean existsByReceiverAndSenderAndStatus(User receiver, User sender, FriendRequestStatus status);

        Optional<FriendRequest> findBySenderAndReceiverOrSenderAndReceiver(
                        User sender1,
                        User receiver1,
                        User sender2,
                        User receiver2);
        List<FriendRequest> findBySenderIdAndStatus(UUID senderId, FriendRequestStatus status);

        List<FriendRequest> findByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

        int countByReceiverIdAndStatus(UUID receiverId, FriendRequestStatus status);

        boolean existsBySenderAndReceiverAndStatusIn(User sender, User receiver, List<FriendRequestStatus> statuses);

        void deleteBySenderAndReceiver(User sender, User receiver);
}