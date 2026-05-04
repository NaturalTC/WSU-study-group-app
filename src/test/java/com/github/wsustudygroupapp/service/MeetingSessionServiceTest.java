package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingSessionServiceTest {

    @Mock private MeetingSessionRepository meetingSessionRepository;
    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private MeetingSessionService meetingSessionService;

    private static final String EMAIL = "test@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

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
        mockProfile.setUser(mockUser);

        otherProfile = new Profile();
        otherProfile.setId(99L);

        mockGroup = new StudyGroup();
        mockGroup.setId(5L);
        mockGroup.setName("Algorithms Study Group");
        mockGroup.setMembers(List.of(mockProfile, otherProfile));

        mockSession = new MeetingSession();
        mockSession.setId(10L);
        mockSession.setStudyGroup(mockGroup);
        mockSession.setScheduledBy(mockProfile);
        mockSession.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        mockSession.setLocation("Ely Library Rm 204");
        mockSession.setNotes("Review chapters 5-7");

        sessionRequest = new MeetingSessionRequest();
        sessionRequest.setGroupId(5L);
        sessionRequest.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        sessionRequest.setLocation("Ely Library Rm 204");
        sessionRequest.setNotes("Review chapters 5-7");
    }

    // stubs the currentProfile() helper used by most service methods
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
    void scheduleSession_setsAllFieldsOnNewSession() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        MeetingSession result = meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        assertEquals(mockGroup, result.getStudyGroup());
        assertEquals(mockProfile, result.getScheduledBy());
        assertEquals(sessionRequest.getScheduledAt(), result.getScheduledAt());
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
    void scheduleSession_notifiesGroupMembers() {
        stubCurrentProfile();
        when(studyGroupRepository.findById(5L)).thenReturn(Optional.of(mockGroup));
        when(meetingSessionRepository.save(any(MeetingSession.class))).thenReturn(mockSession);

        meetingSessionService.scheduleSession(EMAIL, sessionRequest);

        verify(notificationService, times(1)).notifyGroupMembers(eq(mockGroup), anyString(), any(), eq(10L), eq(1L));
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
    void getSessionsForGroup_returnsSessionsOrderedByTime() {
        when(meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(5L))
                .thenReturn(List.of(mockSession));

        List<MeetingSession> result = meetingSessionService.getSessionsForGroup(5L);

        assertEquals(1, result.size());
        assertEquals(mockSession, result.get(0));
    }

    @Test
    void getSessionsForGroup_noSessions_returnsEmptyList() {
        when(meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(5L))
                .thenReturn(List.of());

        assertTrue(meetingSessionService.getSessionsForGroup(5L).isEmpty());
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
    void cancelSession_notTheCreator_doesNotDelete() {
        stubCurrentProfile();
        mockSession.setScheduledBy(otherProfile);
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        assertThrows(IllegalArgumentException.class,
                () -> meetingSessionService.cancelSession(10L, EMAIL));
        verify(meetingSessionRepository, never()).delete(any());
    }

    @Test
    void cancelSession_ownSession_deletesCorrectRecord() {
        stubCurrentProfile();
        when(meetingSessionRepository.findById(10L)).thenReturn(Optional.of(mockSession));

        meetingSessionService.cancelSession(10L, EMAIL);

        verify(meetingSessionRepository, times(1)).delete(mockSession);
    }
}
