package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findTop10ByFullNameContainingIgnoreCase(String keyword);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByFullNameContainingIgnoreCase(String keyword);

    List<User> findTop10ByFullNameContainingIgnoreCaseAndIdNotIn(
            String keyword,
            List<UUID> ids
    );
}