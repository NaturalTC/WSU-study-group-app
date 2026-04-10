package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.service.ChatService;
import com.github.wsustudygroupapp.service.StudyGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final ChatService chatService;

    public StudyGroupController(StudyGroupService studyGroupService, ChatService chatService) {
        this.studyGroupService = studyGroupService;
        this.chatService = chatService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudyGroup>> getGroupsForCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(studyGroupService.getGroupsForCourse(courseId));
    }

    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(
            @Valid @RequestBody StudyGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyGroupService.createGroup(request, userDetails.getUsername()));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<StudyGroup> joinGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyGroupService.joinGroup(groupId, userDetails.getUsername()));
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        studyGroupService.leaveGroup(groupId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getHistory(groupId));
    }
}
