package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.NotificationResponse;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.*;
import com.github.wsustudygroupapp.model.Notification.NotificationType;
import com.github.wsustudygroupapp.repository.NotificationRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private NotificationService notificationService;

    private static final String EMAIL = "student@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

    private User mockUser;
    private Profile mockProfile;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setName("Alex Rivera");
        mockProfile.setUser(mockUser);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Stubs the user + profile lookup for EMAIL so currentProfile() succeeds. */
    private void stubCurrentProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
    }

    private Notification buildNotification(Long id, Profile recipient, boolean isRead) {
        Notification n = new Notification();
        n.setId(id);
        n.setRecipient(recipient);
        n.setMessage("Test notification");
        n.setType(NotificationType.SESSION_SCHEDULED);
        n.setRelatedEntityId(10L);
        n.setRead(isRead);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    // ── getNotifications ──────────────────────────────────────────────────────

    @Test
    void getNotifications_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotifications(UNKNOWN_EMAIL));
    }

    @Test
    void getNotifications_profileNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotifications(EMAIL));
    }

    @Test
    void getNotifications_returnsAllNotificationsAsDtos() {
        stubCurrentProfile();
        Notification n1 = buildNotification(1L, mockProfile, false);
        Notification n2 = buildNotification(2L, mockProfile, true);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n1, n2));

        List<NotificationResponse> result = notificationService.getNotifications(EMAIL);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getNotifications_mapsAllDtoFieldsCorrectly() {
        stubCurrentProfile();
        Notification n = buildNotification(5L, mockProfile, false);
        n.setMessage("Jordan scheduled a session");
        n.setType(NotificationType.SESSION_SCHEDULED);
        n.setRelatedEntityId(42L);
        LocalDateTime ts = LocalDateTime.of(2026, 4, 12, 9, 30);
        n.setCreatedAt(ts);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(n));

        NotificationResponse dto = notificationService.getNotifications(EMAIL).get(0);

        assertEquals(5L, dto.getId());
        assertEquals("Jordan scheduled a session", dto.getMessage());
        assertEquals(NotificationType.SESSION_SCHEDULED, dto.getType());
        assertEquals(42L, dto.getRelatedEntityId());
        assertFalse(dto.isRead());
        assertEquals(ts, dto.getCreatedAt());
    }

    @Test
    void getNotifications_emptyList_returnsEmptyList() {
        stubCurrentProfile();
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        assertTrue(notificationService.getNotifications(EMAIL).isEmpty());
    }

    // ── getUnreadCount ────────────────────────────────────────────────────────

    @Test
    void getUnreadCount_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getUnreadCount(UNKNOWN_EMAIL));
    }

    @Test
    void getUnreadCount_returnsRepositoryCount() {
        stubCurrentProfile();
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(3);

        assertEquals(3, notificationService.getUnreadCount(EMAIL));
    }

    @Test
    void getUnreadCount_noUnread_returnsZero() {
        stubCurrentProfile();
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(0);

        assertEquals(0, notificationService.getUnreadCount(EMAIL));
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_notificationNotFound_throwsResourceNotFoundException() {
        stubCurrentProfile();
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAsRead(99L, EMAIL));
    }

    @Test
    void markAsRead_notificationBelongsToAnotherStudent_throws403() {
        stubCurrentProfile();
        Profile otherProfile = new Profile();
        otherProfile.setId(99L);
        Notification n = buildNotification(1L, otherProfile, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(1L, EMAIL));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void markAsRead_ownNotification_setsReadTrue() {
        stubCurrentProfile();
        Notification n = buildNotification(1L, mockProfile, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(1L, EMAIL);

        assertTrue(n.isRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_savesExactlyOnce() {
        stubCurrentProfile();
        Notification n = buildNotification(1L, mockProfile, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(1L, EMAIL);

        verify(notificationRepository, times(1)).save(n);
    }

    // ── markAllAsRead ─────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.markAllAsRead(UNKNOWN_EMAIL));
    }

    @Test
    void markAllAsRead_setsReadTrueOnAllUnread() {
        stubCurrentProfile();
        Notification n1 = buildNotification(1L, mockProfile, false);
        Notification n2 = buildNotification(2L, mockProfile, false);
        when(notificationRepository.findByRecipientIdAndIsReadFalse(1L))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(EMAIL);

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
    }

    @Test
    void markAllAsRead_savesAllUpdatedNotifications() {
        stubCurrentProfile();
        Notification n1 = buildNotification(1L, mockProfile, false);
        Notification n2 = buildNotification(2L, mockProfile, false);
        when(notificationRepository.findByRecipientIdAndIsReadFalse(1L))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(EMAIL);

        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    void markAllAsRead_noUnread_saveAllCalledWithEmptyList() {
        stubCurrentProfile();
        when(notificationRepository.findByRecipientIdAndIsReadFalse(1L))
                .thenReturn(List.of());

        notificationService.markAllAsRead(EMAIL);

        verify(notificationRepository).saveAll(List.of());
    }

    // ── notifyGroupMembers ────────────────────────────────────────────────────

    @Test
    void notifyGroupMembers_createsNotificationForEachNonExcludedMember() {
        Profile member1 = new Profile(); member1.setId(1L);
        Profile member2 = new Profile(); member2.setId(2L);
        Profile member3 = new Profile(); member3.setId(3L);

        StudyGroup group = new StudyGroup();
        group.setId(10L);
        group.setMembers(List.of(member1, member2, member3));

        notificationService.notifyGroupMembers(group, "Session at 3pm",
                NotificationType.SESSION_SCHEDULED, 10L, 1L);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        // member1 (id=1) is excluded — only member2 and member3 should be notified
        assertEquals(2, saved.size());
        assertTrue(saved.stream().noneMatch(n -> n.getRecipient().getId().equals(1L)));
    }

    @Test
    void notifyGroupMembers_setsCorrectFieldsOnEachNotification() {
        Profile member = new Profile(); member.setId(2L);
        StudyGroup group = new StudyGroup();
        group.setMembers(List.of(member));

        notificationService.notifyGroupMembers(group, "New session",
                NotificationType.SESSION_SCHEDULED, 42L, 99L);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        Notification n = captor.getValue().get(0);
        assertEquals(member, n.getRecipient());
        assertEquals("New session", n.getMessage());
        assertEquals(NotificationType.SESSION_SCHEDULED, n.getType());
        assertEquals(42L, n.getRelatedEntityId());
        assertFalse(n.isRead());
    }

    @Test
    void notifyGroupMembers_allMembersExcluded_savesEmptyList() {
        Profile member = new Profile(); member.setId(1L);
        StudyGroup group = new StudyGroup();
        group.setMembers(List.of(member));

        notificationService.notifyGroupMembers(group, "msg",
                NotificationType.MEMBER_JOINED, 1L, 1L);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertTrue(captor.getValue().isEmpty());
    }

    // ── notifySessionScheduled ────────────────────────────────────────────────

    @Test
    void notifySessionScheduled_buildsMessageWithSchedulerNameGroupNameAndDate() {
        Profile scheduler = new Profile(); scheduler.setId(1L); scheduler.setName("Jordan Smith");
        Profile member = new Profile(); member.setId(2L);

        StudyGroup group = new StudyGroup();
        group.setId(10L);
        group.setName("CS 201 Study Crew");
        group.setMembers(List.of(scheduler, member));

        MeetingSession session = new MeetingSession();
        session.setId(5L);
        session.setScheduledBy(scheduler);
        session.setStudyGroup(group);
        session.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));

        notificationService.notifySessionScheduled(session);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        String message = captor.getValue().get(0).getMessage();
        assertTrue(message.contains("Jordan Smith"));
        assertTrue(message.contains("CS 201 Study Crew"));
        assertTrue(message.contains("2026-05-10"));
    }

    @Test
    void notifySessionScheduled_doesNotNotifyScheduler() {
        Profile scheduler = new Profile(); scheduler.setId(1L); scheduler.setName("Jordan Smith");
        Profile member = new Profile(); member.setId(2L);

        StudyGroup group = new StudyGroup();
        group.setId(10L);
        group.setName("CS 201 Study Crew");
        group.setMembers(List.of(scheduler, member));

        MeetingSession session = new MeetingSession();
        session.setId(5L);
        session.setScheduledBy(scheduler);
        session.setStudyGroup(group);
        session.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));

        notificationService.notifySessionScheduled(session);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        assertTrue(captor.getValue().stream()
                .noneMatch(n -> n.getRecipient().getId().equals(scheduler.getId())));
    }

    @Test
    void notifySessionScheduled_usesSessionScheduledType() {
        Profile scheduler = new Profile(); scheduler.setId(1L); scheduler.setName("Jordan Smith");
        Profile member = new Profile(); member.setId(2L);

        StudyGroup group = new StudyGroup();
        group.setId(10L);
        group.setName("CS 201 Study Crew");
        group.setMembers(List.of(scheduler, member));

        MeetingSession session = new MeetingSession();
        session.setId(5L);
        session.setScheduledBy(scheduler);
        session.setStudyGroup(group);
        session.setScheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));

        notificationService.notifySessionScheduled(session);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        assertEquals(NotificationType.SESSION_SCHEDULED, captor.getValue().get(0).getType());
    }
}
