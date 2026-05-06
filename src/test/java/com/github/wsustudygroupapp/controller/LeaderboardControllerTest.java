package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.LeaderboardEntryDTO;
import com.github.wsustudygroupapp.service.GamificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Tests LeaderboardController by calling its methods directly (no server, no Spring context).
// Follows the same pattern as CourseControllerTest — plain Mockito, no @WebMvcTest.
@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock private GamificationService gamificationService;
    @InjectMocks private LeaderboardController leaderboardController;

    // ── GET /leaderboard ──────────────────────────────────────────────────────

    @Test
    void getGlobalLeaderboard_returns200() {
        when(gamificationService.getGlobalLeaderboard(25)).thenReturn(List.of());

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getGlobalLeaderboard(25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getGlobalLeaderboard_returnsServiceResult() {
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, 1L, "Alice", 100, 2, null);
        when(gamificationService.getGlobalLeaderboard(25)).thenReturn(List.of(entry));

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getGlobalLeaderboard(25);

        assertEquals(1, response.getBody().size());
        assertEquals("Alice", response.getBody().get(0).getDisplayName());
        assertEquals(100, response.getBody().get(0).getPoints());
    }

    @Test
    void getGlobalLeaderboard_passesTopParamToService() {
        when(gamificationService.getGlobalLeaderboard(10)).thenReturn(List.of());

        leaderboardController.getGlobalLeaderboard(10);

        verify(gamificationService, times(1)).getGlobalLeaderboard(10);
    }

    @Test
    void getGlobalLeaderboard_emptyLeaderboard_returns200WithEmptyList() {
        when(gamificationService.getGlobalLeaderboard(25)).thenReturn(List.of());

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getGlobalLeaderboard(25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── GET /leaderboard/course/{courseId} ────────────────────────────────────

    @Test
    void getCourseLeaderboard_returns200() {
        when(gamificationService.getCourseLeaderboard(42L, 25)).thenReturn(List.of());

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getCourseLeaderboard(42L, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCourseLeaderboard_returnsServiceResult() {
        LeaderboardEntryDTO entry = new LeaderboardEntryDTO(1, 2L, "Bob", 200, 3, null);
        when(gamificationService.getCourseLeaderboard(42L, 25)).thenReturn(List.of(entry));

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getCourseLeaderboard(42L, 25);

        assertEquals(1, response.getBody().size());
        assertEquals("Bob", response.getBody().get(0).getDisplayName());
    }

    @Test
    void getCourseLeaderboard_passesCourseIdAndTopToService() {
        when(gamificationService.getCourseLeaderboard(42L, 5)).thenReturn(List.of());

        leaderboardController.getCourseLeaderboard(42L, 5);

        verify(gamificationService, times(1)).getCourseLeaderboard(42L, 5);
    }

    @Test
    void getCourseLeaderboard_noEnrollments_returns200WithEmptyList() {
        when(gamificationService.getCourseLeaderboard(99L, 25)).thenReturn(List.of());

        ResponseEntity<List<LeaderboardEntryDTO>> response =
                leaderboardController.getCourseLeaderboard(99L, 25);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
