package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Database operations for the Badge entity.
 * Badges are pre-seeded — this repo is mostly used for lookups during award checks.
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /** Finds a badge by its unique name. Used by GamificationService to look up badges to award. */
    Optional<Badge> findByName(String name);

    // TODO: Maicheal Shenouda — add findByBadgeType() once the BadgeType enum is added to Badge
}
