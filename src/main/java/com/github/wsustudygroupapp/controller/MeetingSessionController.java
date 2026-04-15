package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.service.MeetingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Hayden Parker — exposes meeting session endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/meetings")
public class MeetingSessionController {

    private final MeetingSessionService meetingSessionService;

    public MeetingSessionController(MeetingSessionService meetingSessionService) {
        this.meetingSessionService = meetingSessionService;
    }

    @PostMapping
    public ResponseEntity<MeetingSession> scheduleSession(@RequestBody MeetingSessionRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        MeetingSession session = meetingSessionService.scheduleSession(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingSession>> getUpcomingSessions(@AuthenticationPrincipal UserDetails userDetails) {
        List<MeetingSession> sessions = meetingSessionService.getUpcomingSessions(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MeetingSession>> getSessionsForGroup(@PathVariable Long groupId) {
        List<MeetingSession> sessions = meetingSessionService.getSessionsForGroup(groupId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        meetingSessionService.cancelSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
