package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MeetingRescheduleRequest;
import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.*;
import com.github.wsustudygroupapp.repository.MeetingSessionRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles scheduling, retrieval, and cancellation of study group meeting sessions.
 * When a session is created, all group members (except the creator) are notified automatically.
 */
@Service
public class MeetingSessionService {

    private final MeetingSessionRepository meetingSessionRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MeetingSessionService(MeetingSessionRepository meetingSessionRepository,
                                 StudyGroupRepository studyGroupRepository,
                                 ProfileRepository profileRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.meetingSessionRepository = meetingSessionRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new meeting session for a group and notifies all other members.
     * Enforces:
     *   • the scheduler is a member of the group (otherwise 403)
     *   • the requested time is in the future (defense in depth — DTO already has @Future)
     */
    public MeetingSession scheduleSession(String schedulerEmail, MeetingSessionRequest request) {
        Profile scheduler = currentProfile(schedulerEmail);
        StudyGroup group = studyGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + request.getGroupId()));

        requireGroupMember(group, scheduler, "schedule meetings in");

        if (request.getScheduledAt() == null || !request.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Session must be scheduled for a future time");
        }

        MeetingSession session = new MeetingSession();
        session.setStudyGroup(group);
        session.setScheduledBy(scheduler);
        session.setScheduledAt(request.getScheduledAt());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setLocation(request.getLocation());
        session.setNotes(request.getNotes());

        MeetingSession savedSession = meetingSessionRepository.save(session);

        String message = "New study session scheduled for " + request.getScheduledAt()
                + (request.getLocation() != null && !request.getLocation().isBlank()
                        ? " at " + request.getLocation() : "");
        notificationService.notifyGroupMembers(group, message, Notification.NotificationType.SESSION_SCHEDULED,
                savedSession.getId(), scheduler.getId());

        return savedSession;
    }

    /** Returns all future sessions across every group the student belongs to, ordered by date. */
    public List<MeetingSession> getUpcomingSessions(String email) {
        Profile profile = currentProfile(email);
        return meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
                profile.getId(), LocalDateTime.now());
    }

    /**
     * Returns all sessions for a specific group, ordered by scheduled time.
     * Only members of the group may view its sessions — otherwise 403.
     */
    public List<MeetingSession> getSessionsForGroup(Long groupId, String requestingEmail) {
        Profile profile = currentProfile(requestingEmail);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));
        requireGroupMember(group, profile, "view meetings for");
        return meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(groupId);
    }

    /**
     * Updates an existing session. Only the creator can edit, and only before the session
     * has started. Behavior depends on which fields are populated in the request:
     * <ul>
     *   <li>{@code scheduledAt} non-null → reschedules to the new time (must be in the future)
     *       and sends a {@code SESSION_RESCHEDULED} notification to other members.</li>
     *   <li>{@code scheduledAt} null → leaves the time alone, treats this as a details-only
     *       edit (location/notes/duration). No reschedule notification.</li>
     * </ul>
     * Any of {@code location}, {@code notes}, {@code durationMinutes} that are non-null are applied;
     * null fields leave the existing value unchanged.
     */
    public MeetingSession rescheduleSession(Long sessionId, String requestingEmail, MeetingRescheduleRequest request) {
        Profile requester = currentProfile(requestingEmail);
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting session not found: " + sessionId));

        if (!session.getScheduledBy().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Only the session creator can edit it");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!session.getScheduledAt().isAfter(now)) {
            throw new IllegalArgumentException("Cannot edit a session that has already started");
        }

        boolean rescheduling = request.getScheduledAt() != null;
        LocalDateTime previous = session.getScheduledAt();

        if (rescheduling) {
            if (!request.getScheduledAt().isAfter(now)) {
                throw new IllegalArgumentException("New scheduled time must be in the future");
            }
            session.setScheduledAt(request.getScheduledAt());
        }
        if (request.getLocation()        != null) session.setLocation(request.getLocation());
        if (request.getNotes()           != null) session.setNotes(request.getNotes());
        if (request.getDurationMinutes() != null) session.setDurationMinutes(request.getDurationMinutes());

        MeetingSession saved = meetingSessionRepository.save(session);

        if (rescheduling) {
            String message = requester.getName() + " rescheduled a session for "
                    + session.getStudyGroup().getName() + ": was " + previous + ", now " + request.getScheduledAt();
            notificationService.notifyGroupMembers(session.getStudyGroup(), message,
                    Notification.NotificationType.SESSION_RESCHEDULED, saved.getId(), requester.getId());
        }

        return saved;
    }

    /**
     * Deletes a session. Only the student who originally scheduled it can cancel.
     * Notifies all other group members so the meeting drops off their lists.
     */
    public void cancelSession(Long sessionId, String requestingEmail) {
        Profile requester = currentProfile(requestingEmail);
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting session not found: " + sessionId));

        if (!session.getScheduledBy().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Only the session creator can cancel it");
        }

        StudyGroup group = session.getStudyGroup();
        LocalDateTime when = session.getScheduledAt();
        Long sessionIdSnapshot = session.getId();

        meetingSessionRepository.delete(session);

        String message = requester.getName() + " cancelled the session for "
                + group.getName() + " scheduled for " + when;
        notificationService.notifyGroupMembers(group, message,
                Notification.NotificationType.SESSION_CANCELLED, sessionIdSnapshot, requester.getId());
    }

    // throws 403 if the profile is not a member of the group
    private void requireGroupMember(StudyGroup group, Profile profile, String actionPhrase) {
        boolean isMember = group.getMembers() != null
                && group.getMembers().stream().anyMatch(m -> m.getId().equals(profile.getId()));
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You must be a member of this group to " + actionPhrase + " it");
        }
    }

    // resolves the logged-in user's Profile from their email — throws 404 if not found
    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
