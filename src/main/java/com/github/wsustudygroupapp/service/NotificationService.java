package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.NotificationResponse;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Badge;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.model.Notification;
import com.github.wsustudygroupapp.model.Notification.NotificationType;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.NotificationRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Creates and delivers in-app notifications to students.
 * Notifications are triggered by backend events (session scheduled, badge earned, member joined)
 * and fetched by the frontend on page load to populate the bell dropdown.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               ProfileRepository profileRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /** Returns all notifications for the logged-in student, newest first. Used to populate the bell dropdown. */
    public List<NotificationResponse> getNotifications(String email) {
        Profile profile = currentProfile(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Returns the count of unread notifications. Used to show the red dot on the bell icon. */
    public int getUnreadCount(String email) {
        Profile profile = currentProfile(email);
        return notificationRepository.countByRecipientIdAndIsReadFalse(profile.getId());
    }

    /** Marks a single notification as read. Throws 403 if the requesting student doesn't own it. */
    public void markAsRead(Long notificationId, String email) {
        Profile profile = currentProfile(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /** Deletes all notifications for the logged-in student. */
    @Transactional
    public void clearAll(String email) {
        Profile profile = currentProfile(email);
        notificationRepository.deleteByRecipientId(profile.getId());
    }

    /** Marks all of a student's unread notifications as read. */
    public void markAllAsRead(String email) {
        Profile profile = currentProfile(email);
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(profile.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Creates a notification for every member of a group except the excluded profile.
     * Used to avoid notifying the student who triggered the event.
     */
    public void notifyGroupMembers(StudyGroup group, String message, NotificationType type,
                                   Long relatedEntityId, Long excludedProfileId) {
        List<Notification> notifications = group.getMembers().stream()
                .filter(member -> !member.getId().equals(excludedProfileId))
                .map(member -> {
                    Notification n = new Notification();
                    n.setRecipient(member);
                    n.setMessage(message);
                    n.setType(type);
                    n.setRelatedEntityId(relatedEntityId);
                    n.setRead(false);
                    return n;
                })
                .toList();
        notificationRepository.saveAll(notifications);
    }

    /** Notifies the recipient that they earned a badge. */
    public void notifyBadgeEarned(Profile recipient, Badge badge) {
        saveNotification(recipient, "You earned the '" + badge.getName() + "' badge!",
                NotificationType.BADGE_EARNED, badge.getId());
    }

    /** Notifies all existing group members (except the joiner) that someone new joined. */
    public void notifyMemberJoined(StudyGroup group, Profile joiner) {
        notifyGroupMembers(group, joiner.getName() + " joined " + group.getName(),
                NotificationType.MEMBER_JOINED, group.getId(), joiner.getId());
        saveNotification(joiner, "You joined " + group.getName(),
                NotificationType.MEMBER_JOINED, group.getId());
    }

    /** Notifies the recipient that they have a new direct message from sender. */
    public void notifyDirectMessage(Profile recipient, Profile sender) {
        saveNotification(recipient,
                sender.getName() + " sent you a message",
                NotificationType.DIRECT_MESSAGE,
                sender.getId());
    }

    /** Notifies all group members (except the scheduler) that a new session was scheduled. */
    public void notifySessionScheduled(MeetingSession session) {
        Profile scheduler = session.getScheduledBy();
        StudyGroup group = session.getStudyGroup();
        String othersMessage = scheduler.getName() + " scheduled a session for "
                + group.getName() + " on " + session.getScheduledAt().toLocalDate();
        notifyGroupMembers(group, othersMessage, NotificationType.SESSION_SCHEDULED,
                session.getId(), scheduler.getId());
        saveNotification(scheduler, "You scheduled a session for "
                + group.getName() + " on " + session.getScheduledAt().toLocalDate(),
                NotificationType.SESSION_SCHEDULED, session.getId());
    }

    private void saveNotification(Profile recipient, String message, NotificationType type, Long relatedEntityId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setMessage(message);
        n.setType(type);
        n.setRelatedEntityId(relatedEntityId);
        n.setRead(false);
        notificationRepository.save(n);
    }

    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }

    /** Maps a Notification entity to a NotificationResponse DTO. */
    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getType(),
                n.getRelatedEntityId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
