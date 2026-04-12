package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.NotificationResponse;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.model.Notification;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.NotificationRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Brian Torres — creates and delivers in-app notifications to students
// getNotifications()         → return all notifications for the logged-in student
// getUnreadCount()           → return the count of unread notifications (for the bell badge)
// markAsRead()               → mark a single notification as read
// markAllAsRead()            → mark all of a student's notifications as read
// notifyGroupMembers()       → create a notification for every member of a group (except one excluded profile)
// notifySessionScheduled()   → convenience method: notifies all group members a session was scheduled

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               ProfileRepository profileRepository) {
        this.notificationRepository = notificationRepository;
        this.profileRepository = profileRepository;
    }

    // TODO: call notificationRepository.findByRecipientIdOrderByCreatedAtDesc(profileId)
    // TODO: map each Notification to a NotificationResponse DTO and return the list
    public List<NotificationResponse> getNotifications(Long profileId) {
        return null;
    }

    // TODO: return notificationRepository.countByRecipientIdAndIsReadFalse(profileId)
    public int getUnreadCount(Long profileId) {
        return 0;
    }

    // TODO: find the notification by notificationId — throw ResourceNotFoundException if missing
    // TODO: verify notification.getRecipient().getId() equals requestingProfileId — throw 403 if not
    // TODO: set notification.setRead(true) and save
    public void markAsRead(Long notificationId, Long requestingProfileId) {

    }

    // TODO: call notificationRepository.findByRecipientIdAndIsReadFalse(profileId)
    // TODO: set isRead = true on each one and save all
    public void markAllAsRead(Long profileId) {

    }

    // TODO: for each member in group.getMembers() whose id is NOT excludedProfileId:
    //       build a new Notification with message, recipient = member, isRead = false, createdAt = now
    //       save each notification
    public void notifyGroupMembers(StudyGroup group, String message, Long excludedProfileId) {

    }

    // TODO: build the notification message (e.g. "<name> scheduled a session for <group> on <date>")
    // TODO: call notifyGroupMembers() with all members except the scheduler
    public void notifySessionScheduled(MeetingSession session) {

    }
}
