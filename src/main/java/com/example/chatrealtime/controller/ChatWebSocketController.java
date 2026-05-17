package com.example.chatrealtime.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.chatrealtime.dto.request.SendMessageRequest;
import com.example.chatrealtime.service.MessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequest request) {
        messageService.sendMessage(request);
    }


}
