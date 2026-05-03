package com.github.wsustudygroupapp.dto;

import com.github.wsustudygroupapp.model.Notification.NotificationType;
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

    /** Auto-generated primary key — used by the frontend to mark this notification as read. */
    @Schema(description = "Notification ID — used to mark it as read", example = "14")
    private Long id;

    /** The human-readable message shown in the notification dropdown. */
    @Schema(description = "Human-readable message shown in the notification dropdown", example = "Jordan scheduled a session for CS 201")
    private String message;

    /** Categorizes the notification so the frontend can render a different icon per type. */
    @Schema(description = "Categorizes the notification so the frontend can render a different icon per type", example = "SESSION_SCHEDULED")
    private NotificationType type;

    /** ID of the related entity (group, session, badge) for deep-linking on the frontend. */
    @Schema(description = "ID of the related entity (group, session, badge) for deep-linking on the frontend", example = "7")
    private Long relatedEntityId;

    /** False until the student opens the notification. Used to show the unread indicator dot on the bell. */
    @Schema(description = "False until the student opens the notification", example = "false")
    private boolean isRead;

    /** When this notification was created. */
    @Schema(description = "When the notification was created", example = "2026-04-12T09:30:00")
    private LocalDateTime createdAt;
}
