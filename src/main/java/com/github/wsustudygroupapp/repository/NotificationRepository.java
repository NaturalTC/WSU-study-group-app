package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the Notification entity.
 * Notifications are always scoped to a specific recipient — never fetched globally.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Returns all notifications for a student, newest first. Used to populate the bell dropdown. */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long profileId);

    /** Returns only unread notifications. Used to calculate the unread badge count. */
    List<Notification> findByRecipientIdAndIsReadFalse(Long profileId);

    /** Counts unread notifications. Used to show the red dot on the notification bell. */
    int countByRecipientIdAndIsReadFalse(Long profileId);
}
