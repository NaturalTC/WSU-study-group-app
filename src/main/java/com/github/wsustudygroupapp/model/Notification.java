package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An in-app notification delivered to a specific student.
 * Examples: "Jordan scheduled a session for CS 201", "You earned the 'Group Starter' badge".
 * Notifications are created by the backend and read by the frontend on page load.
 */
@Entity
@Table(name = "notification_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public enum NotificationType {
        SESSION_SCHEDULED,
        /** An existing session was moved to a different time by its creator. */
        SESSION_RESCHEDULED,
        /** A scheduled session was cancelled by its creator. */
        SESSION_CANCELLED,
        BADGE_EARNED,
        MEMBER_JOINED,
        DIRECT_MESSAGE
    }

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student this notification is addressed to. */
    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile recipient;

    /** The human-readable message shown in the notification bell (e.g. "New session scheduled"). */
    @Column(nullable = false)
    private String message;

    /** Categorizes the notification so the frontend can render a different icon per type. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** ID of the related entity (group, session, badge) for deep-linking on the frontend. */
    @Column
    private Long relatedEntityId;

    /** False until the student opens the notification. Used to show the unread badge count. */
    @Column(nullable = false)
    private boolean isRead = false;

    /** When this notification was created. Set automatically on insert. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
