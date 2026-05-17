package com.example.chatrealtime.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.AddParticipantRequest;
import com.example.chatrealtime.dto.request.CreateConversationRequest;
import com.example.chatrealtime.dto.response.ApiResponse;
import com.example.chatrealtime.dto.response.ConversationResponse;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.service.ConversationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public List<ConversationSummaryResponse> getMyConversations() {
        return conversationService.getMyConversations();
    }

    @PostMapping
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request) {

        return conversationService.createConversation(request);
    }

    @PostMapping("/participants")
    public void addParticipant(
            @RequestBody AddParticipantRequest request) {
        conversationService.addParticipant(request);
    }
}
