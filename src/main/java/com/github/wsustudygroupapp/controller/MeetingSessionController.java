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



/**
 * REST endpoints for scheduling, viewing, and cancelling study group meeting sessions.
 * All endpoints require a valid JWT — the logged-in user's email is extracted from the token.
 */
@RestController
@RequestMapping("/meetings")
public class MeetingSessionController {

    private final MeetingSessionService meetingSessionService;

    public MeetingSessionController(MeetingSessionService meetingSessionService) {
        this.meetingSessionService = meetingSessionService;
    }

    /** POST /meetings — schedule a new session for a study group (201 Created). */
    @PostMapping
    public ResponseEntity<MeetingSession> scheduleSession(@RequestBody MeetingSessionRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        MeetingSession session = meetingSessionService.scheduleSession(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    /** GET /meetings/upcoming — returns all future sessions across the student's groups (200 OK). */
    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingSession>> getUpcomingSessions(@AuthenticationPrincipal UserDetails userDetails) {
        List<MeetingSession> sessions = meetingSessionService.getUpcomingSessions(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    /** GET /meetings/group/{groupId} — returns all sessions for a specific group (200 OK). */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MeetingSession>> getSessionsForGroup(@PathVariable Long groupId) {
        List<MeetingSession> sessions = meetingSessionService.getSessionsForGroup(groupId);
        return ResponseEntity.ok(sessions);
    }

    /** DELETE /meetings/{sessionId} — cancel a session (only the creator can do this, 204 No Content). */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        meetingSessionService.cancelSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
