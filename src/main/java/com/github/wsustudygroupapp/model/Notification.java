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

    /** False until the student opens the notification. Used to show the unread badge count. */
    @Column(nullable = false)
    private boolean isRead = false;

    /** When this notification was created. Set automatically on insert. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // TODO: Brian Torres — add a NotificationType enum (SESSION_SCHEDULED, BADGE_EARNED, MEMBER_JOINED)
    //       so the frontend can render a different icon per type
    // TODO: Brian Torres — add an optional relatedEntityId field to deep-link
    //       (e.g. clicking a SESSION_SCHEDULED notification opens that group's chat)
}
