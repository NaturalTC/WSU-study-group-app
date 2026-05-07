package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MeetingRescheduleRequest;
import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.service.MeetingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Meetings", description = "Schedule, view, reschedule, and cancel study group meeting sessions")
@RestController
@RequestMapping("/meetings")
public class MeetingSessionController {

    private final MeetingSessionService meetingSessionService;

    public MeetingSessionController(MeetingSessionService meetingSessionService) {
        this.meetingSessionService = meetingSessionService;
    }

    /** POST /meetings — schedule a new session for a study group (201 Created). */
    @Operation(summary = "Schedule a new meeting session for a study group")
    @PostMapping
    public ResponseEntity<MeetingSession> scheduleSession(@Valid @RequestBody MeetingSessionRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        MeetingSession session = meetingSessionService.scheduleSession(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    /** GET /meetings/upcoming — returns all future sessions across the student's groups (200 OK). */
    @Operation(summary = "Get all upcoming sessions across all groups the student belongs to")
    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingSession>> getUpcomingSessions(@AuthenticationPrincipal UserDetails userDetails) {
        List<MeetingSession> sessions = meetingSessionService.getUpcomingSessions(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    /** GET /meetings/group/{groupId} — returns all sessions for a specific group (200 OK).
     *  Caller must be a member of the group, otherwise 403. */
    @Operation(summary = "Get all sessions for a specific study group (members only)")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MeetingSession>> getSessionsForGroup(@PathVariable Long groupId,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        List<MeetingSession> sessions = meetingSessionService.getSessionsForGroup(groupId, userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    /** PATCH /meetings/{sessionId} — reschedule an upcoming session (only the creator, 200 OK). */
    @Operation(summary = "Reschedule or edit a meeting session (creator only)")
    @PatchMapping("/{sessionId}")
    public ResponseEntity<MeetingSession> rescheduleSession(@PathVariable Long sessionId,
                                                            @Valid @RequestBody MeetingRescheduleRequest request,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        MeetingSession session = meetingSessionService.rescheduleSession(sessionId, userDetails.getUsername(), request);
        return ResponseEntity.ok(session);
    }

    /** DELETE /meetings/{sessionId} — cancel a session (only the creator can do this, 204 No Content). */
    @Operation(summary = "Cancel a meeting session (creator only)")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        meetingSessionService.cancelSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
