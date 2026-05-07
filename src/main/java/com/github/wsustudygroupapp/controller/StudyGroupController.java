package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.JoinGroupRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroup> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(studyGroupService.getGroupById(groupId));
    }

    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllGroups() {
        return ResponseEntity.ok(studyGroupService.getAllGroups());
    }

    // get all groups for a course (consider adding paging params)
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudyGroup>> getGroupsForCourse(@PathVariable Long courseId) {
        List<StudyGroup> groups = studyGroupService.getGroupsForCourse(courseId);
        return ResponseEntity.ok(groups);
    }

    // create a new group
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(
            @Valid @RequestBody StudyGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyGroup createdGroup = studyGroupService.createGroup(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<StudyGroup> joinGroup(@PathVariable Long groupId,
                                                @RequestBody(required = false) JoinGroupRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        String password = request != null ? request.getPassword() : null;
        StudyGroup updatedGroup = studyGroupService.joinGroup(groupId, password, userDetails.getUsername());
        return ResponseEntity.ok(updatedGroup);
    }

    // leave a group
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        studyGroupService.leaveGroup(groupId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // upload or replace the group cover picture — only the creator can do this
    @PostMapping("/{groupId}/picture")
    public ResponseEntity<StudyGroup> uploadGroupPicture(@PathVariable Long groupId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(studyGroupService.uploadGroupPicture(groupId, userDetails.getUsername(), file));
    }

    // delete a group — only the creator can do this
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        studyGroupService.deleteGroup(groupId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // load chat history when user opens the group chat (consider pagination)
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getHistory(groupId));
    }

    // get all groups the logged-in student is a member of
    @GetMapping("/my")
    public ResponseEntity<List<StudyGroup>> getMyGroups(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyGroupService.getMyGroups(userDetails.getUsername()));
    }
}
