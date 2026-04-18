package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.UserBadge;
import com.github.wsustudygroupapp.repository.BadgeRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Maicheal Shenouda — handles points, badges, and leaderboard logic
// awardPoints()         → add points to a student's profile and check badge eligibility
// awardBadge()          → grant a badge to a student (idempotent — safe to call repeatedly)
// checkBadgeEligibility() → run all badge checks after a point-earning action
// getGlobalLeaderboard()  → return top N students across the whole app ranked by points
// getCourseLeaderboard()  → return top N students in a specific course ranked by points

@Service
public class GamificationService {

    private final ProfileRepository profileRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public GamificationService(ProfileRepository profileRepository,
                               BadgeRepository badgeRepository,
                               UserBadgeRepository userBadgeRepository) {
        this.profileRepository = profileRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    // TODO: find the profile by profileId — throw ResourceNotFoundException if missing
    // TODO: add the points amount to profile.getPoints()
    // TODO: save the profile
    // TODO: call checkBadgeEligibility(profile) to see if any badge thresholds were crossed
    public void awardPoints(Long profileId, int points) {

    }

    // TODO: look up the badge by name — throw ResourceNotFoundException if missing
    // TODO: check userBadgeRepository.existsByProfileIdAndBadgeId() — return early if already earned
    // TODO: build a new UserBadge linking the profile and badge, set awardedAt = now
    // TODO: save the UserBadge
    // TODO: add badge.getPointValue() to profile.getPoints() and save
    public void awardBadge(Long profileId, String badgeName) {

    }

    // TODO: call awardBadge() for each badge whose threshold the profile now meets
    //       Example thresholds to implement:
    //       - "First Group Join"   → earned when profile joins their first group
    //       - "Group Starter"      → earned when profile creates their first group
    //       - "Active Chatter"     → earned when profile sends 10 messages
    //       - "Session Scheduler"  → earned when profile schedules their first meeting
    //       - "Point Milestone 100" → earned when profile's points reach 100
    private void checkBadgeEligibility(Profile profile) {

    }

    // TODO: load all profiles, sort by points descending, take top N
    // TODO: for each profile build a LeaderboardEntryDTO with rank, profileId, displayName, points, badgeCount
    // TODO: return the list
    public List<LeaderboardEntryDTO> getGlobalLeaderboard(int topN) {
        return null;
    }

    // TODO: load all profiles enrolled in a given course (via UserCourse), sort by points descending, take top N
    // TODO: build and return LeaderboardEntryDTO list the same way as getGlobalLeaderboard()
    public List<LeaderboardEntryDTO> getCourseLeaderboard(Long courseId, int topN) {
        return null;
    }
}
