package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.*;
import com.github.wsustudygroupapp.repository.*;
import com.github.wsustudygroupapp.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Unit tests for GamificationService. All repositories are mocked (no real database needed).
// See GamificationService.java for the implementation being tested.
@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private BadgeRepository badgeRepository;
    @Mock private UserBadgeRepository userBadgeRepository;
    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private MeetingSessionRepository meetingSessionRepository;
    @Mock private UserCourseRepository userCourseRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private GamificationService gamificationService;

    private Profile mockProfile;
    private Badge mockBadge;

    @BeforeEach
    void setUp() {
        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setName("Hayden Parker");
        mockProfile.setPoints(0);

        mockBadge = new Badge();
        mockBadge.setId(10L);
        mockBadge.setName("First Group Join");
        mockBadge.setPointValue(20);
    }

    // Stubs all activity counts to zero so checkBadgeEligibility does nothing.
    // Use in tests that don't care about badge checks.
    private void stubNoBadgeEligibility() {
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of());
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(0);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);
    }

    // ── awardPoints ───────────────────────────────────────────────────────────

    @Test
    void awardPoints_profileNotFound_throwsResourceNotFoundException() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gamificationService.awardPoints(99L, 10));
    }

    @Test
    void awardPoints_addsCorrectPointsToProfile() {
        mockProfile.setPoints(50);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        stubNoBadgeEligibility();

        gamificationService.awardPoints(1L, 30);

        assertEquals(80, mockProfile.getPoints());
    }

    @Test
    void awardPoints_savesProfileExactlyOnce() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        stubNoBadgeEligibility();

        gamificationService.awardPoints(1L, 10);

        verify(profileRepository, times(1)).save(mockProfile);
    }

    // ── awardBadge ────────────────────────────────────────────────────────────

    @Test
    void awardBadge_badgeNotFound_throwsResourceNotFoundException() {
        when(badgeRepository.findByName("Ghost Badge")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gamificationService.awardBadge(1L, "Ghost Badge"));
    }

    @Test
    void awardBadge_profileNotFound_throwsResourceNotFoundException() {
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.of(mockBadge));
        when(userBadgeRepository.existsByProfileIdAndBadgeId(1L, 10L)).thenReturn(false);
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gamificationService.awardBadge(1L, "First Group Join"));
    }

    @Test
    void awardBadge_alreadyEarned_returnsEarlyWithoutSaving() {
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.of(mockBadge));
        when(userBadgeRepository.existsByProfileIdAndBadgeId(1L, 10L)).thenReturn(true);

        gamificationService.awardBadge(1L, "First Group Join");

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void awardBadge_alreadyEarned_doesNotChangePoints() {
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.of(mockBadge));
        when(userBadgeRepository.existsByProfileIdAndBadgeId(1L, 10L)).thenReturn(true);

        gamificationService.awardBadge(1L, "First Group Join");

        verify(profileRepository, never()).save(any());
        assertEquals(0, mockProfile.getPoints());
    }

    @Test
    void awardBadge_newBadge_savesUserBadge() {
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.of(mockBadge));
        when(userBadgeRepository.existsByProfileIdAndBadgeId(1L, 10L)).thenReturn(false);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));

        gamificationService.awardBadge(1L, "First Group Join");

        verify(userBadgeRepository, times(1)).save(any(UserBadge.class));
    }

    @Test
    void awardBadge_newBadge_creditsPointValueToProfile() {
        mockBadge.setPointValue(20);
        mockProfile.setPoints(10);
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.of(mockBadge));
        when(userBadgeRepository.existsByProfileIdAndBadgeId(1L, 10L)).thenReturn(false);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));

        gamificationService.awardBadge(1L, "First Group Join");

        assertEquals(30, mockProfile.getPoints());
        verify(profileRepository, atLeastOnce()).save(mockProfile);
    }

    // ── checkBadgeEligibility (tested via awardPoints) ────────────────────────
    // For each threshold test: stubs the relevant count, returns Optional.empty()
    // for the badge so tryAwardBadge silently skips — we just verify the lookup happened.

    @Test
    void awardPoints_joinedOneGroup_attemptsFirstGroupJoinBadge() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of(new StudyGroup()));
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(0);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.empty());

        gamificationService.awardPoints(1L, 10);

        verify(badgeRepository).findByName("First Group Join");
    }

    @Test
    void awardPoints_createdOneGroup_attemptsGroupStarterBadge() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of());
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of(new StudyGroup()));
        when(messageRepository.countBySenderId(1L)).thenReturn(0);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);
        when(badgeRepository.findByName("Group Starter")).thenReturn(Optional.empty());

        gamificationService.awardPoints(1L, 10);

        verify(badgeRepository).findByName("Group Starter");
    }

    @Test
    void awardPoints_sentTenMessages_attemptsActiveChatterBadge() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of());
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(10);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);
        when(badgeRepository.findByName("Active Chatter")).thenReturn(Optional.empty());

        gamificationService.awardPoints(1L, 1);

        verify(badgeRepository).findByName("Active Chatter");
    }

    @Test
    void awardPoints_nineMessages_doesNotAttemptActiveChatterBadge() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of());
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(9);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);

        gamificationService.awardPoints(1L, 1);

        verify(badgeRepository, never()).findByName("Active Chatter");
    }

    @Test
    void awardPoints_scheduledSession_attemptsSessionSchedulerBadge() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of());
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(0);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(1);
        when(badgeRepository.findByName("Session Scheduler")).thenReturn(Optional.empty());

        gamificationService.awardPoints(1L, 1);

        verify(badgeRepository).findByName("Session Scheduler");
    }

    @Test
    void awardPoints_reachedHundredPoints_attemptsPointMilestoneBadge() {
        mockProfile.setPoints(99);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        stubNoBadgeEligibility();
        when(badgeRepository.findByName("Point Milestone 100")).thenReturn(Optional.empty());

        gamificationService.awardPoints(1L, 1);

        verify(badgeRepository).findByName("Point Milestone 100");
    }

    @Test
    void awardPoints_missingBadgeInDb_doesNotThrow() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(studyGroupRepository.findByMembersId(1L)).thenReturn(List.of(new StudyGroup()));
        when(studyGroupRepository.findByCreatedById(1L)).thenReturn(List.of());
        when(messageRepository.countBySenderId(1L)).thenReturn(0);
        when(meetingSessionRepository.countByScheduledById(1L)).thenReturn(0);
        when(badgeRepository.findByName("First Group Join")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> gamificationService.awardPoints(1L, 10));
    }

    // ── getGlobalLeaderboard ──────────────────────────────────────────────────

    @Test
    void getGlobalLeaderboard_noProfiles_returnsEmptyList() {
        when(profileRepository.findAll()).thenReturn(List.of());

        assertTrue(gamificationService.getGlobalLeaderboard(10).isEmpty());
    }

    @Test
    void getGlobalLeaderboard_sortsByPointsDescending() {
        when(profileRepository.findAll()).thenReturn(List.of(
                makeProfile(1L, "Alice", 100),
                makeProfile(2L, "Bob", 500),
                makeProfile(3L, "Carol", 300)
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getGlobalLeaderboard(10);

        assertEquals("Bob",   result.get(0).getDisplayName());
        assertEquals("Carol", result.get(1).getDisplayName());
        assertEquals("Alice", result.get(2).getDisplayName());
    }

    @Test
    void getGlobalLeaderboard_ranksAreOneBased() {
        when(profileRepository.findAll()).thenReturn(List.of(
                makeProfile(1L, "Alice", 200),
                makeProfile(2L, "Bob", 100)
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getGlobalLeaderboard(10);

        assertEquals(1, result.get(0).getRank());
        assertEquals(2, result.get(1).getRank());
    }

    @Test
    void getGlobalLeaderboard_respectsTopNLimit() {
        when(profileRepository.findAll()).thenReturn(List.of(
                makeProfile(1L, "A", 500),
                makeProfile(2L, "B", 400),
                makeProfile(3L, "C", 300)
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getGlobalLeaderboard(2);

        assertEquals(2, result.size());
    }

    @Test
    void getGlobalLeaderboard_includesBadgeCountPerProfile() {
        when(profileRepository.findAll()).thenReturn(List.of(makeProfile(1L, "Alice", 100)));
        when(userBadgeRepository.countByProfileId(1L)).thenReturn(3);

        List<LeaderboardEntryDTO> result = gamificationService.getGlobalLeaderboard(10);

        assertEquals(3, result.get(0).getBadgeCount());
    }

    // ── getCourseLeaderboard ──────────────────────────────────────────────────

    @Test
    void getCourseLeaderboard_noEnrollments_returnsEmptyList() {
        when(userCourseRepository.findByCourseId(42L)).thenReturn(List.of());

        assertTrue(gamificationService.getCourseLeaderboard(42L, 10).isEmpty());
    }

    @Test
    void getCourseLeaderboard_sortsByPointsDescending() {
        Profile p1 = makeProfile(1L, "Alice", 50);
        Profile p2 = makeProfile(2L, "Bob", 200);
        when(userCourseRepository.findByCourseId(42L)).thenReturn(List.of(
                makeUserCourse(p1), makeUserCourse(p2)
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getCourseLeaderboard(42L, 10);

        assertEquals("Bob",   result.get(0).getDisplayName());
        assertEquals("Alice", result.get(1).getDisplayName());
    }

    @Test
    void getCourseLeaderboard_respectsTopNLimit() {
        when(userCourseRepository.findByCourseId(42L)).thenReturn(List.of(
                makeUserCourse(makeProfile(1L, "A", 300)),
                makeUserCourse(makeProfile(2L, "B", 200)),
                makeUserCourse(makeProfile(3L, "C", 100))
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getCourseLeaderboard(42L, 2);

        assertEquals(2, result.size());
    }

    @Test
    void getCourseLeaderboard_deduplicatesProfileEnrolledInMultipleSections() {
        Profile p1 = makeProfile(1L, "Alice", 100);
        when(userCourseRepository.findByCourseId(42L)).thenReturn(List.of(
                makeUserCourse(p1), makeUserCourse(p1)
        ));
        when(userBadgeRepository.countByProfileId(anyLong())).thenReturn(0);

        List<LeaderboardEntryDTO> result = gamificationService.getCourseLeaderboard(42L, 10);

        assertEquals(1, result.size());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Profile makeProfile(Long id, String name, int points) {
        Profile p = new Profile();
        p.setId(id);
        p.setName(name);
        p.setPoints(points);
        return p;
    }

    private UserCourse makeUserCourse(Profile profile) {
        UserCourse uc = new UserCourse();
        uc.setProfile(profile);
        return uc;
    }
}
