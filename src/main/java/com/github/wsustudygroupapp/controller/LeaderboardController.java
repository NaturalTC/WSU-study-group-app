package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes two read-only leaderboard endpoints:
 *   GET /leaderboard                      → top students across the whole app
 *   GET /leaderboard/course/{courseId}    → top students in a specific course
 *
 * Both endpoints accept an optional ?top=N query param (default 25) that controls
 * how many rows are returned. All routes require a valid JWT token (enforced by
 * Spring Security config — not visible here).
 */
@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final GamificationService gamificationService;

    public LeaderboardController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    // Returns the top N students app-wide, ranked by total points.
    // ?top=N is optional — defaults to 25 if the frontend doesn't provide it.
    // TODO [DONE]: call gamificationService.getGlobalLeaderboard(topN)
    // TODO [DONE]: return 200 with the ranked list
    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "25") int top) {
        return ResponseEntity.ok(gamificationService.getGlobalLeaderboard(top));
    }

    // Returns the top N students enrolled in the given course, ranked by total points.
    // courseId comes from the URL path (e.g. /leaderboard/course/42).
    // TODO [DONE]: call gamificationService.getCourseLeaderboard(courseId, topN)
    // TODO [DONE]: return 200 with the ranked list filtered to students in that course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getCourseLeaderboard(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "25") int top) {
        return ResponseEntity.ok(gamificationService.getCourseLeaderboard(courseId, top));
    }
}
