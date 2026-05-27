package com.example.chatrealtime.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.dto.request.AddParticipantRequest;
import com.example.chatrealtime.dto.request.CreateConversationRequest;
import com.example.chatrealtime.dto.request.UpdateConversationResquest;
import com.example.chatrealtime.dto.response.ConversationResponse;
import com.example.chatrealtime.dto.response.ConversationSummaryResponse;
import com.example.chatrealtime.dto.response.ParticipantResponse;
import com.example.chatrealtime.dto.response.UserResponse;
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

    @PostMapping("/addGroup/{conversationId}")
    public void addParticipant(
            @RequestBody List<AddParticipantRequest> request, @PathVariable UUID conversationId) {
        conversationService.addParticipant(request, conversationId);
    }

    @PutMapping("/lastreadat/{conversationId}")
    public void updateLastReadAt(@PathVariable UUID conversationId) {
        conversationService.updateLastReadAt(conversationId);
    }

    @GetMapping("/{conversationId}")
    public List<ParticipantResponse> getConversationParticipants(@PathVariable UUID conversationId) {
        return conversationService.getConversationParticipants(conversationId);
    }

    @DeleteMapping("/leavegroup/{conversationId}")
    public void leaveConversationGroup(@PathVariable UUID conversationId) {
        conversationService.leaveConversationGroup(conversationId);
    }

    @DeleteMapping("/kick/{conversationId}/{userId}")
    public void kickMemberGroup(@PathVariable UUID conversationId, @PathVariable UUID userId) {
        conversationService.kickMemberGroup(conversationId, userId);
    }

    @PutMapping("/setroleadmin/{conversationId}/{userId}")
    public void setRoleAdmin(@PathVariable UUID conversationId, @PathVariable UUID userId) {
        conversationService.setRoleAdmin(conversationId, userId);
    }

    @GetMapping("/friendnotingroup/{conversationId}")
    public List<UserResponse> getFriendsNotInGroup(@PathVariable UUID conversationId) {
        return conversationService.getFriendsNotInGroup(conversationId);
    }

    @PutMapping("/updategroup/{conversationId}")
    public void updateGroupInfo(@PathVariable UUID conversationId, @RequestBody UpdateConversationResquest request) {
        conversationService.updateGroupInfo(request, conversationId);
    }

    @DeleteMapping("/removegroup/{conversationId}")
    public void removeGroupChat(@PathVariable UUID conversationId) {
        conversationService.removeGroupChat(conversationId);
    }
}
