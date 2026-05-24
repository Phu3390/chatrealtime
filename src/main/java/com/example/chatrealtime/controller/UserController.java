package com.example.chatrealtime.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.UpdateProfileRequest;
import com.example.chatrealtime.dto.response.FriendRequestResponse;
import com.example.chatrealtime.dto.response.UserResponse;
import com.example.chatrealtime.enums.FriendRequestStatus;
import com.example.chatrealtime.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search")
    public List<UserResponse> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }

    @GetMapping("/friendstatus")
    public List<FriendRequestResponse> getFriendStatus(@RequestParam FriendRequestStatus status) {
        return userService.getFriendStatus(status);
    }

    @GetMapping("/friendinvitations")
    public List<FriendRequestResponse> getListFriendInvitation() {
        return userService.getListFriendInvitation();
    }

    @GetMapping("/friendinvitations/count")
    public int countFriendInvitation() {
        return userService.countFriendInvitation();
    }

    @PutMapping("/update/profile")
    public UserResponse updateProfile(@RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }
    
}
