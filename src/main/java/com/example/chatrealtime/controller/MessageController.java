package com.example.chatrealtime.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.SendMessageRequest;
import com.example.chatrealtime.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/send")
    public void sendMessageTest(@RequestBody SendMessageRequest request) {
        messageService.sendMessage(request);
    }
}
