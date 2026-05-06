package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// All routes here require a valid JWT token (enforced by Spring Security config).
@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final GamificationService gamificationService;

    public LeaderboardController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    // TODO [DONE]: call gamificationService.getGlobalLeaderboard(topN)
    // TODO [DONE]: return 200 with the ranked list
    // Default topN = 25 — frontend can override with ?top=50 etc.
    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "25") int top) {
        return ResponseEntity.ok(gamificationService.getGlobalLeaderboard(top));
    }

    // TODO [DONE]: call gamificationService.getCourseLeaderboard(courseId, topN)
    // TODO [DONE]: return 200 with the ranked list filtered to students in that course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getCourseLeaderboard(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "25") int top) {
        return ResponseEntity.ok(gamificationService.getCourseLeaderboard(courseId, top));
    }
}
