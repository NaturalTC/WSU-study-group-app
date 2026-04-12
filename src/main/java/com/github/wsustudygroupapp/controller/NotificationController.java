package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.NotificationResponse;
import com.github.wsustudygroupapp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Brian Torres — exposes notification endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call notificationService.getNotifications(profileId)
    // TODO: return 200 with the list of NotificationResponse DTOs
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call notificationService.getUnreadCount(profileId)
    // TODO: return 200 with the integer count
    // Used by the frontend to show the red dot number on the notification bell
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call notificationService.markAsRead(notificationId, profileId)
    // TODO: return 204 No Content
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId,
                                           Authentication authentication) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID from the Authentication principal
    // TODO: call notificationService.markAllAsRead(profileId)
    // TODO: return 204 No Content
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        return null;
    }
}
