package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MeetingRescheduleRequest;
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
import org.springframework.web.server.ResponseStatusException;

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
    private static final LocalDateTime FUTURE     = LocalDateTime.of(2030, 1, 1, 14, 0);
    private static final LocalDateTime FAR_FUTURE = LocalDateTime.of(2030, 6, 1, 14, 0);

    private MeetingSession mockSession;
    private MeetingSessionRequest sessionRequest;
    private MeetingRescheduleRequest rescheduleRequest;

    @BeforeEach
    void setUp() {
        mockSession = new MeetingSession();
        mockSession.setId(10L);
        mockSession.setScheduledAt(FUTURE);
        mockSession.setDurationMinutes(60);
        mockSession.setLocation("Ely Library Rm 204");
        mockSession.setNotes("Review chapters 5-7");

        sessionRequest = new MeetingSessionRequest();
        sessionRequest.setGroupId(5L);
        sessionRequest.setScheduledAt(FUTURE);
        sessionRequest.setDurationMinutes(60);
        sessionRequest.setLocation("Ely Library Rm 204");
        sessionRequest.setNotes("Review chapters 5-7");

        rescheduleRequest = new MeetingRescheduleRequest();
        rescheduleRequest.setScheduledAt(FAR_FUTURE);
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
    void scheduleSession_forwardsAuthenticatedEmailToService() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest)).thenReturn(mockSession);

        meetingSessionController.scheduleSession(sessionRequest, userDetails);

        verify(meetingSessionService).scheduleSession(EMAIL, sessionRequest);
    }

    @Test
    void scheduleSession_serviceThrowsNotFound_propagates() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest))
                .thenThrow(new ResourceNotFoundException("Study group not found: 5"));

        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionController.scheduleSession(sessionRequest, userDetails));
    }

    @Test
    void scheduleSession_serviceThrowsForbidden_propagates() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member"));

        assertThrows(ResponseStatusException.class,
                () -> meetingSessionController.scheduleSession(sessionRequest, userDetails));
    }

    @Test
    void scheduleSession_serviceThrowsIllegalArgument_propagates() {
        mockAuth();
        when(meetingSessionService.scheduleSession(EMAIL, sessionRequest))
                .thenThrow(new IllegalArgumentException("Session must be scheduled for a future time"));

        assertThrows(IllegalArgumentException.class,
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

    @Test
    void getUpcomingSessions_forwardsAuthenticatedEmailToService() {
        mockAuth();
        when(meetingSessionService.getUpcomingSessions(EMAIL)).thenReturn(List.of());

        meetingSessionController.getUpcomingSessions(userDetails);

        verify(meetingSessionService).getUpcomingSessions(EMAIL);
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

    @Test
    void getSessionsForGroup_forwardsGroupIdAndEmailToService() {
        mockAuth();
        when(meetingSessionService.getSessionsForGroup(5L, EMAIL)).thenReturn(List.of());

        meetingSessionController.getSessionsForGroup(5L, userDetails);

        verify(meetingSessionService).getSessionsForGroup(5L, EMAIL);
    }

    @Test
    void getSessionsForGroup_serviceThrowsForbidden_propagates() {
        mockAuth();
        when(meetingSessionService.getSessionsForGroup(5L, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member"));

        assertThrows(ResponseStatusException.class,
                () -> meetingSessionController.getSessionsForGroup(5L, userDetails));
    }

    // ── PATCH /meetings/{sessionId} ───────────────────────────────────────────

    @Test
    void rescheduleSession_returns200WithUpdatedSession() {
        mockAuth();
        when(meetingSessionService.rescheduleSession(10L, EMAIL, rescheduleRequest))
                .thenReturn(mockSession);

        ResponseEntity<MeetingSession> response =
                meetingSessionController.rescheduleSession(10L, rescheduleRequest, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSession, response.getBody());
    }

    @Test
    void rescheduleSession_forwardsIdEmailAndRequestToService() {
        mockAuth();
        when(meetingSessionService.rescheduleSession(10L, EMAIL, rescheduleRequest))
                .thenReturn(mockSession);

        meetingSessionController.rescheduleSession(10L, rescheduleRequest, userDetails);

        verify(meetingSessionService).rescheduleSession(10L, EMAIL, rescheduleRequest);
    }

    @Test
    void rescheduleSession_serviceThrowsNotFound_propagates() {
        mockAuth();
        when(meetingSessionService.rescheduleSession(10L, EMAIL, rescheduleRequest))
                .thenThrow(new ResourceNotFoundException("Meeting session not found: 10"));

        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionController.rescheduleSession(10L, rescheduleRequest, userDetails));
    }

    @Test
    void rescheduleSession_serviceThrowsIllegalArgument_propagates() {
        mockAuth();
        when(meetingSessionService.rescheduleSession(10L, EMAIL, rescheduleRequest))
                .thenThrow(new IllegalArgumentException("Only the session creator can edit it"));

        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionController.rescheduleSession(10L, rescheduleRequest, userDetails));
    }

    // ── DELETE /meetings/{sessionId} ──────────────────────────────────────────

    @Test
    void cancelSession_returns204NoContent() {
        mockAuth();
        doNothing().when(meetingSessionService).cancelSession(10L, EMAIL);

        ResponseEntity<Void> response = meetingSessionController.cancelSession(10L, userDetails);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void cancelSession_forwardsIdAndEmailToService() {
        mockAuth();
        doNothing().when(meetingSessionService).cancelSession(10L, EMAIL);

        meetingSessionController.cancelSession(10L, userDetails);

        verify(meetingSessionService).cancelSession(10L, EMAIL);
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
