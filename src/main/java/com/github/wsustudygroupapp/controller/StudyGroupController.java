package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.service.ChatService;
import com.github.wsustudygroupapp.service.StudyGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Hayden — exposes study group endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final ChatService chatService;

    public StudyGroupController(StudyGroupService studyGroupService, ChatService chatService) {
        this.studyGroupService = studyGroupService;
        this.chatService = chatService;
    }

    // TODO: call studyGroupService.getGroupsForCourse(courseId)
    // TODO: return 200 with the list of groups
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudyGroup>> getGroupsForCourse(@PathVariable Long courseId) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call studyGroupService.createGroup(profileId, request)
    // TODO: return 201 with the created group
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroupRequest request) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call studyGroupService.joinGroup(groupId, profileId)
    // TODO: return 200 with the updated group
    @PostMapping("/{groupId}/join")
    public ResponseEntity<StudyGroup> joinGroup(@PathVariable Long groupId) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call studyGroupService.leaveGroup(groupId, profileId)
    // TODO: return 204 No Content
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        return null;
    }

    // TODO: call chatService.getHistory(groupId)
    // TODO: return 200 with message history (loaded when student opens the chat)
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long groupId) {
        return null;
    }
}
