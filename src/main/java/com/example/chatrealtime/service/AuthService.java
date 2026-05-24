package com.example.chatrealtime.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.dto.request.LoginRequest;
import com.example.chatrealtime.dto.request.RegisterRequest;
import com.example.chatrealtime.dto.response.AuthResponse;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.UserStatus;
import com.example.chatrealtime.global.dto.ErrorCode;
import com.example.chatrealtime.global.exception.AppException;
import com.example.chatrealtime.mapper.UserMapper;
import com.example.chatrealtime.repository.UserRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthService {
    final UserRepository repository;
    final PasswordEncoder passwordEncoder;
    final UserMapper userMapper;

    @NonFinal
    @Value("${jwt.secret}")
    String SECRET_KEY;

    @NonFinal
    @Value("${jwt.expiration}")
    int exp;

    public UUID extractUserId(String token) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String userIdStr = (String) jwsObject.getPayload().toJSONObject().get("userId");
            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_PASSWORD_INVALID));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.EMAIL_PASSWORD_INVALID);
        }
        return AuthResponse.builder()
                .token(genarateToken(user))
                .isAuth(true)
                .build();
    }

    @Transactional
    public AuthResponse signup(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXITS);
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(UserStatus.OFFLINE);
        
        User usernew = repository.save(user);
        LoginRequest loginRequest = LoginRequest.builder()
                .email(usernew.getEmail())
                .password(request.getPassword())
                .build();
        return login(loginRequest);
    }

    public UserResponse getMe(){
        User user = repository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
        return userMapper.toResponse(user);
    }

    public String genarateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("phudeptrai.com")
                .issueTime(new java.util.Date())
                .expirationTime(new Date(Instant.now().plus(exp, ChronoUnit.HOURS).toEpochMilli()))
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
