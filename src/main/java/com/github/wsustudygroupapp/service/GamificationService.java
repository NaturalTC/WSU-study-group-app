package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Badge;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.UserBadge;
import com.github.wsustudygroupapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all gamification logic for the app — points, badges, and leaderboard rankings.
 *
 * How it fits together:
 *   - Students earn points by taking actions (creating groups, sending messages, etc.).
 *     Each action calls awardPoints() from the relevant service (StudyGroupService, ChatService, etc.).
 *   - After every point award, checkBadgeEligibility() runs automatically to see if the student
 *     has hit any badge thresholds. If so, awardBadge() grants the badge and adds its bonus points.
 *   - The leaderboard endpoints (LeaderboardController) call getGlobalLeaderboard() or
 *     getCourseLeaderboard() to get a ranked snapshot of students by points.
 *
 * Point values (defined where each action occurs):
 *   +15 create a study group  (StudyGroupService)
 *   +10 join a study group    (StudyGroupService)
 *   + 1 send a chat message   (ChatService)
 *   +25 schedule a meeting    (MeetingSessionService)
 */
@Service
public class GamificationService {

    private final ProfileRepository profileRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MessageRepository messageRepository;
    private final MeetingSessionRepository meetingSessionRepository;
    private final UserCourseRepository userCourseRepository;
    private final NotificationService notificationService;

    public GamificationService(ProfileRepository profileRepository,
                                BadgeRepository badgeRepository,
                                UserBadgeRepository userBadgeRepository,
                                StudyGroupRepository studyGroupRepository,
                                MessageRepository messageRepository,
                                MeetingSessionRepository meetingSessionRepository,
                                UserCourseRepository userCourseRepository,
                                NotificationService notificationService) {
        this.profileRepository = profileRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.messageRepository = messageRepository;
        this.meetingSessionRepository = meetingSessionRepository;
        this.userCourseRepository = userCourseRepository;
        this.notificationService = notificationService;
    }

    // Called by ChatService, StudyGroupService, and MeetingSessionService after the main
    // action (send message, create group, etc.) has already been saved.
    // TODO [DONE]: find the profile by profileId — throw ResourceNotFoundException if missing
    // TODO [DONE]: add the points amount to profile.getPoints()
    // TODO [DONE]: save the profile
    // TODO [DONE]: call checkBadgeEligibility(profile) to see if any badge thresholds were crossed
    @Transactional
    public void awardPoints(Long profileId, int points) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        profile.setPoints(profile.getPoints() + points);
        profileRepository.save(profile);
        checkBadgeEligibility(profile);
    }

    // Safe to call multiple times — if the student already has the badge, returns immediately
    // without making any DB changes (idempotent). When a new badge is granted, its bonus
    // points are added to the student's total and the student receives a notification.
    // TODO [DONE]: look up the badge by name — throw ResourceNotFoundException if missing
    // TODO [DONE]: check userBadgeRepository.existsByProfileIdAndBadgeId() — return early if already earned
    // TODO [DONE]: build a new UserBadge linking the profile and badge, set awardedAt = now
    // TODO [DONE]: save the UserBadge
    // TODO [DONE]: add badge.getPointValue() to profile.getPoints() and save
    void awardBadge(Long profileId, String badgeName) {
        Badge badge = badgeRepository.findByName(badgeName)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found: " + badgeName));
        // return early — prevents awarding the same badge twice
        if (userBadgeRepository.existsByProfileIdAndBadgeId(profileId, badge.getId())) {
            return;
        }
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        UserBadge userBadge = new UserBadge();
        userBadge.setProfile(profile);
        userBadge.setBadge(badge);
        userBadge.setAwardedAt(LocalDateTime.now());
        userBadgeRepository.save(userBadge);
        profile.setPoints(profile.getPoints() + badge.getPointValue());
        profileRepository.save(profile);
        notificationService.notifyBadgeEarned(profile, badge);
    }

    // Runs after every awardPoints() call. Queries the DB to count the student's activity
    // totals, then tries to grant any badge whose threshold has been crossed.
    // awardBadge() handles duplicate-prevention, so calling this repeatedly is safe.
    // TODO [DONE]: call awardBadge() for each badge whose threshold the profile now meets
    //       Thresholds implemented:
    //       - "First Group Join"    → earned when profile joins their first group
    //       - "Group Starter"       → earned when profile creates their first group
    //       - "Active Chatter"      → earned when profile sends 10 messages
    //       - "Session Scheduler"   → earned when profile schedules their first meeting
    //       - "Point Milestone 100" → earned when profile's points reach 100
    private void checkBadgeEligibility(Profile profile) {
        int groupsJoined   = studyGroupRepository.findByMembersId(profile.getId()).size();
        int groupsCreated  = studyGroupRepository.findByCreatedById(profile.getId()).size();
        int messagesSent   = messageRepository.countBySenderId(profile.getId());
        int sessionsBooked = meetingSessionRepository.countByScheduledById(profile.getId());

        if (groupsJoined   >= 1)        tryAwardBadge(profile.getId(), "First Group Join");
        if (groupsCreated  >= 1)        tryAwardBadge(profile.getId(), "Group Starter");
        if (messagesSent   >= 10)       tryAwardBadge(profile.getId(), "Active Chatter");
        if (sessionsBooked >= 1)        tryAwardBadge(profile.getId(), "Session Scheduler");
        if (profile.getPoints() >= 100) tryAwardBadge(profile.getId(), "Point Milestone 100");
    }

    // Wraps awardBadge() and swallows ResourceNotFoundException so checkBadgeEligibility()
    // stays safe even if a badge was never inserted in the database (e.g. a fresh dev environment).
    private void tryAwardBadge(Long profileId, String badgeName) {
        try {
            awardBadge(profileId, badgeName);
        } catch (ResourceNotFoundException ignored) {
        }
    }

    // Returns the top topN students across the whole app ranked by points (highest first).
    // Rank 1 = most points. Called by GET /leaderboard.
    // TODO [DONE]: load all profiles, sort by points descending, take top N
    // TODO [DONE]: for each profile build a LeaderboardEntryDTO with rank, profileId, displayName, points, badgeCount
    // TODO [DONE]: return the list
    public List<LeaderboardEntryDTO> getGlobalLeaderboard(int topN) {
        List<Profile> sorted = profileRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Profile::getPoints).reversed())
                .limit(topN)
                .collect(Collectors.toList());
        return buildEntries(sorted);
    }

    // Same as getGlobalLeaderboard() but scoped to students enrolled in a specific course.
    // A student enrolled in multiple sections appears only once (.distinct() handles that).
    // Called by GET /leaderboard/course/{courseId}.
    // TODO [DONE]: load all profiles enrolled in a given course (via UserCourse), sort by points descending, take top N
    // TODO [DONE]: build and return LeaderboardEntryDTO list the same way as getGlobalLeaderboard()
    public List<LeaderboardEntryDTO> getCourseLeaderboard(Long courseId, int topN) {
        List<Profile> sorted = userCourseRepository.findByCourseId(courseId).stream()
                .map(uc -> uc.getProfile())
                .distinct()
                .sorted(Comparator.comparingInt(Profile::getPoints).reversed())
                .limit(topN)
                .collect(Collectors.toList());
        return buildEntries(sorted);
    }

    // Converts a pre-sorted profile list into LeaderboardEntryDTOs.
    // Rank is 1-based (first entry = rank 1). Badge count is queried live from the DB.
    private List<LeaderboardEntryDTO> buildEntries(List<Profile> profiles) {
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            Profile p = profiles.get(i);
            int badgeCount = userBadgeRepository.countByProfileId(p.getId());
            LeaderboardEntryDTO entry = new LeaderboardEntryDTO(i + 1, p.getId(), p.getName(), p.getPoints(), badgeCount);
            entry.setProfilePicURL(p.getProfilePicURL());
            entries.add(entry);
        }
        return entries;
    }
}
