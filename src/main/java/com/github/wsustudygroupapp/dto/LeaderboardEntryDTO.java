package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single row on the leaderboard, returned by GET /leaderboard.
 * Built by GamificationService — not stored directly in the database.
 * The frontend uses this to render rank, name, points, and badge count per student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single leaderboard entry for one student")
public class LeaderboardEntryDTO {

    @Schema(description = "1-based rank position on the leaderboard", example = "1")
    private int rank;

    @Schema(description = "Profile ID of the student", example = "12")
    private Long profileId;

    @Schema(description = "Display name shown on the leaderboard", example = "Alex Johnson")
    private String displayName;

    @Schema(description = "Total points the student has earned", example = "480")
    private int points;

    @Schema(description = "Total number of badges the student has earned", example = "5")
    private int badgeCount;

    // TODO: Sprint 2 — add a topBadgeIcon field so the frontend can show the student's rarest badge
}
