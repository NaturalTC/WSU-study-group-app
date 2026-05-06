package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MeetingRescheduleRequest;
import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.model.Notification;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.MeetingSessionRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingSessionServiceTest {

    @Mock private MeetingSessionRepository meetingSessionRepository;
    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private GamificationService gamificationService;
    @InjectMocks private MeetingSessionService meetingSessionService;

    private static final String EMAIL = "test@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

    // Static far-future timestamps so reschedule "must be in future" checks pass
    // without depending on the wall-clock — tests stay deterministic year-over-year.
    private static final LocalDateTime FUTURE      = LocalDateTime.of(2030, 1, 1, 14, 0);
    private static final LocalDateTime FAR_FUTURE  = LocalDateTime.of(2030, 6, 1, 14, 0);
    private static final LocalDateTime PAST        = LocalDateTime.of(2020, 1, 1, 14, 0);

    private User mockUser;
    private Profile mockProfile;
    private Profile otherProfile;
    private StudyGroup mockGroup;
    private MeetingSession mockSession;
    private MeetingSessionRequest sessionRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setName("Alex Rivera");
        mockProfile.setUser(mockUser);

        otherProfile = new Profile();
        otherProfile.setId(99L);
        otherProfile.setName("Casey Other");

        mockGroup = new StudyGroup();
        mockGroup.setId(5L);
        mockGroup.setName("Algorithms Study Group");
        mockGroup.setMembers(List.of(mockProfile, otherProfile));

        mockSession = new MeetingSession();
        mockSession.setId(10L);
        mockSession.setStudyGroup(mockGroup);
        mockSession.setScheduledBy(mockProfile);
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
    }

    // stubs the currentProfile() helper used by every public method
    private void stubCurrentProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
    }

    // ── scheduleSession ───────────────────────────────────────────────────────

    @Test
    void scheduleSession_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.scheduleSession(UNKNOWN_EMAIL, sessionRequest));
    }

    @Test
    void scheduleSession_groupNotFound_throwsResourceNotFoundException() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.scheduleSession(EMAIL, sessionRequest));
    }

    @Test
    void scheduleSession_userNotInGroup_throwsForbidden() {
        stubCurrentProfile();
        // group has only otherProfile — current user (id=1) is not a member
        StudyGroup foreignGroup = new StudyGroup();
        foreignGroup.setId(5L);
        foreignGroup.setMembers(List.of(otherProfile));
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(foreignGroup));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingSessionService.scheduleSession(EMAIL, sessionRequest));
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.getStatusCode().value());
    }

    @Test
    void scheduleSession_userNotInGroup_doesNotPersistOrNotify() {
        stubCurrentProfile();
        StudyGroup foreignGroup = new StudyGroup();
        foreignGroup.setId(5L);
        foreignGroup.setMembers(List.of(otherProfile));
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(foreignGroup));

        assertThrows(ResponseStatusException.class,
                () -> meetingSessionService.scheduleSession(EMAIL, sessionRequest));
        verify(meetingSessionRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void scheduleSession_nullScheduledAt_throwsIllegalArgumentException() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        sessionRequest.setScheduledAt(null);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.scheduleSession(EMAIL, sessionRequest));
    }

    @Test
    void scheduleSession_pastScheduledAt_throwsIllegalArgumentException() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        sessionRequest.setScheduledAt(PAST);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.scheduleSession(EMAIL, sessionRequest));
    }

    @Test
    void scheduleSession_setsAllFieldsOnNewSession() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        MeetingSession result = meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        assertEquals(mockGroup, result.getStudyGroup());
        assertEquals(mockProfile, result.getScheduledBy());
        assertEquals(FUTURE, result.getScheduledAt());
        assertEquals(60, result.getDurationMinutes());
        assertEquals("Ely Library Rm 204", result.getLocation());
        assertEquals("Review chapters 5-7", result.getNotes());
    }

    @Test
    void scheduleSession_persistsSessionExactlyOnce() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        verify(meetingSessionRepository, times(1)).save(any(MeetingSession.class));
    }

    @Test
    void scheduleSession_notifiesGroupMembersWithSessionScheduledType() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        verify(notificationService, times(1)).notifyGroupMembers(
                eq(mockGroup), anyString(),
                eq(Notification.NotificationType.SESSION_SCHEDULED),
                eq(10L), eq(1L));
    }

    @Test
    void scheduleSession_notificationMessageOmitsLocationWhenBlank() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);
        sessionRequest.setLocation("");

        meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifyGroupMembers(
                any(), messageCaptor.capture(), any(), any(), any());
        assertFalse(messageCaptor.getValue().contains("at "),
                "Blank location should not produce an 'at ' suffix in the notification");
    }

    @Test
    void scheduleSession_notificationMessageIncludesLocationWhenProvided() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifyGroupMembers(
                any(), messageCaptor.capture(), any(), any(), any());
        assertTrue(messageCaptor.getValue().contains("Ely Library Rm 204"));
    }

    // ── getUpcomingSessions ───────────────────────────────────────────────────

    @Test
    void getUpcomingSessions_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.getUpcomingSessions(UNKNOWN_EMAIL));
    }

    @Test
    void getUpcomingSessions_returnsSessionsFromRepo() {
        stubCurrentProfile();
        when(meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of(mockSession));

        List<MeetingSession> result = meetingSessionService.getUpcomingSessions(EMAIL);

        assertEquals(1, result.size());
        assertEquals(mockSession, result.get(0));
    }

    @Test
    void getUpcomingSessions_noUpcoming_returnsEmptyList() {
        stubCurrentProfile();
        when(meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
                eq(1L), any(LocalDateTime.class))).thenReturn(List.of());

        assertTrue(meetingSessionService.getUpcomingSessions(EMAIL).isEmpty());
    }

    // ── getSessionsForGroup ───────────────────────────────────────────────────

    @Test
    void getSessionsForGroup_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.getSessionsForGroup(5L, UNKNOWN_EMAIL));
    }

    @Test
    void getSessionsForGroup_groupNotFound_throwsResourceNotFoundException() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.getSessionsForGroup(5L, EMAIL));
    }

    @Test
    void getSessionsForGroup_userNotInGroup_throwsForbidden() {
        stubCurrentProfile();
        StudyGroup foreignGroup = new StudyGroup();
        foreignGroup.setId(5L);
        foreignGroup.setMembers(List.of(otherProfile));
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(foreignGroup));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> meetingSessionService.getSessionsForGroup(5L, EMAIL));
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.getStatusCode().value());
    }

    @Test
    void getSessionsForGroup_returnsSessionsOrderedByTime() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(5L))
                .thenReturn(List.of(mockSession));

        List<MeetingSession> result = meetingSessionService.getSessionsForGroup(5L, EMAIL);

        assertEquals(1, result.size());
        assertEquals(mockSession, result.get(0));
    }

    @Test
    void getSessionsForGroup_noSessions_returnsEmptyList() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(5L))
                .thenReturn(List.of());

        assertTrue(meetingSessionService.getSessionsForGroup(5L, EMAIL).isEmpty());
    }

    // ── rescheduleSession ─────────────────────────────────────────────────────

    @Test
    void rescheduleSession_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.rescheduleSession(10L, UNKNOWN_EMAIL, req));
    }

    @Test
    void rescheduleSession_sessionNotFound_throwsResourceNotFoundException() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.empty());
        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.rescheduleSession(10L, EMAIL, req));
    }

    @Test
    void rescheduleSession_notTheCreator_throwsIllegalArgumentException() {
        stubCurrentProfile();
        mockSession.setScheduledBy(otherProfile);
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.rescheduleSession(10L, EMAIL, req));
    }

    @Test
    void rescheduleSession_notTheCreator_doesNotSaveOrNotify() {
        stubCurrentProfile();
        mockSession.setScheduledBy(otherProfile);
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.rescheduleSession(10L, EMAIL, req));
        verify(meetingSessionRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void rescheduleSession_sessionAlreadyStarted_throwsIllegalArgumentException() {
        stubCurrentProfile();
        mockSession.setScheduledAt(PAST); // already-started session
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.rescheduleSession(10L, EMAIL, req));
    }

    @Test
    void rescheduleSession_newScheduledAtInPast_throwsIllegalArgumentException() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(PAST);
        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.rescheduleSession(10L, EMAIL, req));
    }

    @Test
    void rescheduleSession_fullReschedule_updatesScheduledAt() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);

        MeetingSession result = meetingSessionService.rescheduleSession(10L, EMAIL, req);

        assertEquals(FAR_FUTURE, result.getScheduledAt());
    }

    @Test
    void rescheduleSession_fullReschedule_sendsRescheduledNotification() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        meetingSessionService.rescheduleSession(10L, EMAIL, req);

        verify(notificationService, times(1)).notifyGroupMembers(
                eq(mockGroup), anyString(),
                eq(Notification.NotificationType.SESSION_RESCHEDULED),
                eq(10L), eq(1L));
    }

    @Test
    void rescheduleSession_detailsOnlyEdit_doesNotChangeScheduledAt() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(null); // details-only edit
        req.setLocation("Bates Hall Rm 110");
        req.setNotes("Quieter spot");
        req.setDurationMinutes(90);

        MeetingSession result = meetingSessionService.rescheduleSession(10L, EMAIL, req);

        assertEquals(FUTURE, result.getScheduledAt(), "Time must be untouched on details-only edit");
        assertEquals("Bates Hall Rm 110", result.getLocation());
        assertEquals("Quieter spot", result.getNotes());
        assertEquals(90, result.getDurationMinutes());
    }

    @Test
    void rescheduleSession_detailsOnlyEdit_doesNotNotify() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setLocation("Bates Hall Rm 110");

        meetingSessionService.rescheduleSession(10L, EMAIL, req);

        verifyNoInteractions(notificationService);
    }

    @Test
    void rescheduleSession_nullFieldsLeaveExistingValuesUnchanged() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        // empty request — nothing should change
        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        MeetingSession result = meetingSessionService.rescheduleSession(10L, EMAIL, req);

        assertEquals(FUTURE, result.getScheduledAt());
        assertEquals(60, result.getDurationMinutes());
        assertEquals("Ely Library Rm 204", result.getLocation());
        assertEquals("Review chapters 5-7", result.getNotes());
    }

    @Test
    void rescheduleSession_persistsExactlyOnce() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        MeetingRescheduleRequest req = new MeetingRescheduleRequest();
        req.setScheduledAt(FAR_FUTURE);
        meetingSessionService.rescheduleSession(10L, EMAIL, req);

        verify(meetingSessionRepository, times(1)).save(any(MeetingSession.class));
    }

    // ── cancelSession ─────────────────────────────────────────────────────────

    @Test
    void cancelSession_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.cancelSession(10L, UNKNOWN_EMAIL));
    }

    @Test
    void cancelSession_sessionNotFound_throwsResourceNotFoundException() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> meetingSessionService.cancelSession(10L, EMAIL));
    }

    @Test
    void cancelSession_notTheCreator_throwsIllegalArgumentException() {
        stubCurrentProfile();
        mockSession.setScheduledBy(otherProfile);
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.cancelSession(10L, EMAIL));
    }

    @Test
    void cancelSession_notTheCreator_doesNotDeleteOrNotify() {
        stubCurrentProfile();
        mockSession.setScheduledBy(otherProfile);
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.cancelSession(10L, EMAIL));
        verify(meetingSessionRepository, never()).delete(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelSession_ownSession_deletesCorrectRecord() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        meetingSessionService.cancelSession(10L, EMAIL);

        verify(meetingSessionRepository, times(1)).delete(mockSession);
    }

    @Test
    void cancelSession_ownSession_sendsCancelledNotificationWithSnapshottedId() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        meetingSessionService.cancelSession(10L, EMAIL);

        verify(notificationService, times(1)).notifyGroupMembers(
                eq(mockGroup), anyString(),
                eq(Notification.NotificationType.SESSION_CANCELLED),
                eq(10L), eq(1L));
    }
}
