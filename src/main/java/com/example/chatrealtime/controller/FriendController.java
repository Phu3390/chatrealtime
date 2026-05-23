package com.example.chatrealtime.controller;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @DeleteMapping("/remove/{friendId}")
    public void removeFriend(@PathVariable UUID friendId) {
        friendService.removeFriend(friendId);
    }
}
