package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Badge;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.UserBadge;
import com.github.wsustudygroupapp.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GamificationService {

    private final ProfileRepository profileRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MessageRepository messageRepository;
    private final MeetingSessionRepository meetingSessionRepository;
    private final UserCourseRepository userCourseRepository;

    public GamificationService(ProfileRepository profileRepository,
                                BadgeRepository badgeRepository,
                                UserBadgeRepository userBadgeRepository,
                                StudyGroupRepository studyGroupRepository,
                                MessageRepository messageRepository,
                                MeetingSessionRepository meetingSessionRepository,
                                UserCourseRepository userCourseRepository) {
        this.profileRepository = profileRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.messageRepository = messageRepository;
        this.meetingSessionRepository = meetingSessionRepository;
        this.userCourseRepository = userCourseRepository;
    }

    // TODO [DONE]: find the profile by profileId — throw ResourceNotFoundException if missing
    // TODO [DONE]: add the points amount to profile.getPoints()
    // TODO [DONE]: save the profile
    // TODO [DONE]: call checkBadgeEligibility(profile) to see if any badge thresholds were crossed
    public void awardPoints(Long profileId, int points) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        profile.setPoints(profile.getPoints() + points);
        profileRepository.save(profile);
        checkBadgeEligibility(profile);
    }

    // TODO [DONE]: look up the badge by name — throw ResourceNotFoundException if missing
    // TODO [DONE]: check userBadgeRepository.existsByProfileIdAndBadgeId() — return early if already earned
    // TODO [DONE]: build a new UserBadge linking the profile and badge, set awardedAt = now
    // TODO [DONE]: save the UserBadge
    // TODO [DONE]: add badge.getPointValue() to profile.getPoints() and save
    public void awardBadge(Long profileId, String badgeName) {
        Badge badge = badgeRepository.findByName(badgeName)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found: " + badgeName));
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
    }

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

    // Silently skips if the badge hasn't been seeded yet — keeps eligibility checks resilient
    // in environments where not all badges exist in the database.
    private void tryAwardBadge(Long profileId, String badgeName) {
        try {
            awardBadge(profileId, badgeName);
        } catch (ResourceNotFoundException ignored) {
        }
    }

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

    // Converts a pre-sorted profile list into ranked LeaderboardEntryDTOs.
    // Badge count is fetched live from the DB rather than a cached value.
    private List<LeaderboardEntryDTO> buildEntries(List<Profile> profiles) {
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            Profile p = profiles.get(i);
            int badgeCount = userBadgeRepository.countByProfileId(p.getId());
            entries.add(new LeaderboardEntryDTO(i + 1, p.getId(), p.getName(), p.getPoints(), badgeCount));
        }
        return entries;
    }
}
