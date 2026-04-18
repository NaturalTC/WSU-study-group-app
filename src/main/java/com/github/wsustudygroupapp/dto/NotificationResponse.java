package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single notification returned by GET /notifications.
 * Built from a Notification entity — returned as a DTO so we never expose the full entity.
 * The frontend uses isRead to show the unread indicator dot on the notification bell.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single in-app notification for a student")
public class NotificationResponse {

    @Schema(description = "Notification ID — used to mark it as read", example = "14")
    private Long id;

    @Schema(description = "Human-readable message shown in the notification dropdown", example = "Jordan scheduled a session for CS 201")
    private String message;

    @Schema(description = "False until the student opens the notification", example = "false")
    private boolean isRead;

    @Schema(description = "When the notification was created", example = "2026-04-12T09:30:00")
    private LocalDateTime createdAt;

    // TODO: Sprint 2 — add a notificationType field (SESSION_SCHEDULED, BADGE_EARNED, MEMBER_JOINED)
    //       so the frontend can render a different icon per notification type
}
