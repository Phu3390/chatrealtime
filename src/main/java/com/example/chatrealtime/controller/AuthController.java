package com.example.chatrealtime.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.LoginRequest;
import com.example.chatrealtime.dto.request.RegisterRequest;
import com.example.chatrealtime.dto.response.AuthResponse;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.service.AuthService;
import com.example.chatrealtime.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
  

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest payload) {
        return authService.login(payload);
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody RegisterRequest payload) {
        return authService.signup(payload);
    }

    @GetMapping("/me")
    public UserResponse getMe() {
        return authService.getMe();
    }

 

}
