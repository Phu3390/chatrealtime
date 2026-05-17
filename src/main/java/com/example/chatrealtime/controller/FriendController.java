package com.example.chatrealtime.controller;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.FriendRequestAction;
import com.example.chatrealtime.dto.request.SendFriendRequest;
import com.example.chatrealtime.service.FriendService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendService friendService;

    // Gửi lời mời kết bạn
    @PostMapping("/request")
    public void sendFriendRequest(
            @RequestBody SendFriendRequest request) {

        friendService.sendFriendRequest(request);
    }

    @PostMapping("/action")
    public void actionFriendRequest(
            @RequestBody FriendRequestAction request) {

        friendService.actionFriendRequest(request);
    }
}
