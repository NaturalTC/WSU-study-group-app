package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the UserBadge junction table.
 * Used to check which badges a student has earned and to award new ones.
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    /** Returns all badges a student has earned, ordered newest first. Used on their profile page. */
    List<UserBadge> findByProfileIdOrderByAwardedAtDesc(Long profileId);

    /** Returns true if the student already has this badge — prevents awarding it twice. */
    boolean existsByProfileIdAndBadgeId(Long profileId, Long badgeId);

    /** Counts how many badges a student has earned. Used for the leaderboard badge count column. */
    int countByProfileId(Long profileId);
}
