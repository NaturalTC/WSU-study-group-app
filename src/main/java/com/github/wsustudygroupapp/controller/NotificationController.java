package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.NotificationResponse;
import com.github.wsustudygroupapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes notification endpoints for the logged-in student.
 * All routes require a valid JWT token (configured in SecurityConfig).
 * authentication.getName() returns the email stored in the JWT by JwtAuthFilter.
 */
@Tag(name = "Notifications", description = "In-app notifications for the logged-in student")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Returns all notifications for the logged-in student, newest first. */
    @Operation(summary = "Get all notifications for the logged-in student")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getNotifications(authentication.getName()));
    }

    /** Returns the count of unread notifications. Used by the frontend to show the red dot on the bell. */
    @Operation(summary = "Get the count of unread notifications")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getUnreadCount(authentication.getName()));
    }

    /** Marks a single notification as read. Returns 403 if the notification belongs to another student. */
    @Operation(summary = "Mark a single notification as read")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId,
                                           Authentication authentication) {
        notificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /** Marks all of the logged-in student's unread notifications as read. */
    @Operation(summary = "Mark all unread notifications as read")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /** Deletes all notifications for the logged-in student. */
    @Operation(summary = "Delete all notifications for the logged-in student")
    @DeleteMapping
    public ResponseEntity<Void> clearAll(Authentication authentication) {
        notificationService.clearAll(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
