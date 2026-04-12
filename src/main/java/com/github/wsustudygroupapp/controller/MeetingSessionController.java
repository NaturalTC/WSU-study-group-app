package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.service.MeetingSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call meetingSessionService.scheduleSession(profileId, request)
    // TODO: return 201 Created with the new session
    @PostMapping
    public ResponseEntity<MeetingSession> scheduleSession(@RequestBody MeetingSessionRequest request,
                                                          Authentication authentication) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call meetingSessionService.getUpcomingSessions(profileId)
    // TODO: return 200 with the list of upcoming sessions
    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingSession>> getUpcomingSessions(Authentication authentication) {
        return null;
    }

    // TODO: call meetingSessionService.getSessionsForGroup(groupId)
    // TODO: return 200 with the list of sessions
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MeetingSession>> getSessionsForGroup(@PathVariable Long groupId) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call meetingSessionService.cancelSession(sessionId, profileId)
    // TODO: return 204 No Content
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId,
                                              Authentication authentication) {
        return null;
    }
}
