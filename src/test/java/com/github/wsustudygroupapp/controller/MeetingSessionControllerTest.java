package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.service.MeetingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingSessionControllerTest {

    @Mock private MeetingSessionService meetingSessionService;
    @Mock private UserDetails userDetails;
    @InjectMocks private MeetingSessionController meetingSessionController;

    private static final String EMAIL = "test@westfield.ma.edu";
    private MeetingSession mockSession;
    private MeetingSessionRequest sessionRequest;

    @BeforeEach
    void setUp() {
        mockSession = new MeetingSession();
        mockSession.setId(10L);
        mockSession.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        mockSession.setLocation("Ely Library Rm 204");
        mockSession.setNotes("Review chapters 5-7");

        sessionRequest = new MeetingSessionRequest();
        sessionRequest.setGroupId(5L);
        sessionRequest.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        sessionRequest.setLocation("Ely Library Rm 204");
        sessionRequest.setNotes("Review chapters 5-7");
    }

    // stubs userDetails.getUsername() — called per-test to avoid UnnecessaryStubbingException
    private void mockAuth() {
        when(userDetails.getUsername()).thenReturn(EMAIL);
    }

    // ── POST /meetings ────────────────────────────────────────────────────────

    @Test
    void scheduleSession_returns201WithCreatedSession() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest)).thenReturn(mockSession);

        ResponseEntity<MeetingSession> response = meetingSessionController.scheduleSession(sessionRequest, userDetails);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockSession, response.getBody());
    }

    @Test
    void scheduleSession_serviceThrowsNotFound_propagates() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest))
                .thenThrow(new ResourceNotFoundException("Study group not found: 5"));

        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionController.scheduleSession(sessionRequest, userDetails));
    }

    // ── GET /meetings/upcoming ────────────────────────────────────────────────

    @Test
    void getUpcomingSessions_returns200WithSessionList() {
        mockAuth();
        when(meetingSessionService.getUpcomingSessions(EMAIL)).thenReturn(List.of(mockSession));

        ResponseEntity<List<MeetingSession>> response = meetingSessionController.getUpcomingSessions(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getUpcomingSessions_noUpcoming_returns200WithEmptyList() {
        mockAuth();
        when(meetingSessionService.getUpcomingSessions(EMAIL)).thenReturn(List.of());

        ResponseEntity<List<MeetingSession>> response = meetingSessionController.getUpcomingSessions(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── GET /meetings/group/{groupId} ─────────────────────────────────────────

    @Test
    void getSessionsForGroup_returns200WithSessionList() {
        mockAuth();
        when(meetingSessionService.getSessionsForGroup(5L, EMAIL)).thenReturn(List.of(mockSession));

        ResponseEntity<List<MeetingSession>> response = meetingSessionController.getSessionsForGroup(5L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSessionsForGroup_noSessions_returns200WithEmptyList() {
        mockAuth();
        when(meetingSessionService.getSessionsForGroup(5L, EMAIL)).thenReturn(List.of());

        ResponseEntity<List<MeetingSession>> response = meetingSessionController.getSessionsForGroup(5L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── DELETE /meetings/{sessionId} ───────────────────────��──────────────────

    @Test
    void cancelSession_returns204NoContent() {
        mockAuth();
        doNothing().when(meetingSessionService).cancelSession(10L, EMAIL);

        ResponseEntity<Void> response = meetingSessionController.cancelSession(10L, userDetails);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void cancelSession_sessionNotFound_propagates() {
        mockAuth();
        doThrow(new ResourceNotFoundException("Meeting session not found: 10"))
                .when(meetingSessionService).cancelSession(10L, EMAIL);

        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionController.cancelSession(10L, userDetails));
    }

    @Test
    void cancelSession_notTheCreator_propagatesIllegalArgument() {
        mockAuth();
        doThrow(new IllegalArgumentException("Only the session creator can cancel it"))
                .when(meetingSessionService).cancelSession(10L, EMAIL);

        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionController.cancelSession(10L, userDetails));
    }
}
