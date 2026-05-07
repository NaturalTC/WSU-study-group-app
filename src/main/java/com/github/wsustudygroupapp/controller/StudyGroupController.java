package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.JoinGroupRequest;
import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.service.ChatService;
import com.github.wsustudygroupapp.service.StudyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Study Groups", description = "Create, join, leave, and manage study groups")
@RestController
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final ChatService chatService;

    public StudyGroupController(StudyGroupService studyGroupService, ChatService chatService) {
        this.studyGroupService = studyGroupService;
        this.chatService = chatService;
    }

    @Operation(summary = "Get a study group by ID")
    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroup> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(studyGroupService.getGroupById(groupId));
    }

    @Operation(summary = "Get all study groups")
    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllGroups() {
        return ResponseEntity.ok(studyGroupService.getAllGroups());
    }

    // get all groups for a course (consider adding paging params)
    @Operation(summary = "Get all study groups for a specific course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudyGroup>> getGroupsForCourse(@PathVariable Long courseId) {
        List<StudyGroup> groups = studyGroupService.getGroupsForCourse(courseId);
        return ResponseEntity.ok(groups);
    }

    // create a new group
    @Operation(summary = "Create a new study group")
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(
            @Valid @RequestBody StudyGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyGroup createdGroup = studyGroupService.createGroup(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @Operation(summary = "Join a study group")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<StudyGroup> joinGroup(@PathVariable Long groupId,
                                                @RequestBody(required = false) JoinGroupRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        String password = request != null ? request.getPassword() : null;
        StudyGroup updatedGroup = studyGroupService.joinGroup(groupId, password, userDetails.getUsername());
        return ResponseEntity.ok(updatedGroup);
    }

    // leave a group
    @Operation(summary = "Leave a study group")
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        studyGroupService.leaveGroup(groupId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // upload or replace the group cover picture — only the creator can do this
    @Operation(summary = "Upload or replace the group cover picture (creator only)")
    @PostMapping("/{groupId}/picture")
    public ResponseEntity<StudyGroup> uploadGroupPicture(@PathVariable Long groupId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(studyGroupService.uploadGroupPicture(groupId, userDetails.getUsername(), file));
    }

    // delete a group — only the creator can do this
    @Operation(summary = "Delete a study group (creator only)")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        studyGroupService.deleteGroup(groupId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // load chat history when user opens the group chat (consider pagination)
    @Operation(summary = "Get the chat message history for a study group")
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getHistory(groupId));
    }

    // get all groups the logged-in student is a member of
    @Operation(summary = "Get all study groups the logged-in student belongs to")
    @GetMapping("/my")
    public ResponseEntity<List<StudyGroup>> getMyGroups(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyGroupService.getMyGroups(userDetails.getUsername()));
    }
}
