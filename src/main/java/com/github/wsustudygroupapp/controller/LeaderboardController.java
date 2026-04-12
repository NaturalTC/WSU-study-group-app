package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Maicheal Shenouda — exposes leaderboard endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final GamificationService gamificationService;

    public LeaderboardController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    // TODO: call gamificationService.getGlobalLeaderboard(topN)
    // TODO: return 200 with the ranked list
    // Default topN = 25 — frontend can override with ?top=50 etc.
    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "25") int top) {
        return null;
    }

    // TODO: call gamificationService.getCourseLeaderboard(courseId, topN)
    // TODO: return 200 with the ranked list filtered to students in that course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getCourseLeaderboard(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "25") int top) {
        return null;
    }
}
