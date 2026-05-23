package com.example.chatrealtime.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.MessageQuery;
import com.example.chatrealtime.dto.request.SendMessageRequest;
import com.example.chatrealtime.dto.response.MessageResponse;
import com.example.chatrealtime.dto.response.PageResponse;
import com.example.chatrealtime.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/send")
    public MessageResponse sendMessageTest(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/loadmessages")
    public PageResponse<MessageResponse> loadMessages(@ModelAttribute MessageQuery query) {
        return messageService.getMessages(query);
    }
}
