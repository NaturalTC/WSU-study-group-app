package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.MeetingSessionRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    /** Creates a new meeting session for a group and notifies all other members. */
    public MeetingSession scheduleSession(String schedulerEmail, MeetingSessionRequest request) {
        Profile scheduler = currentProfile(schedulerEmail);
        StudyGroup group = studyGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + request.getGroupId()));

        MeetingSession session = new MeetingSession();
        session.setStudyGroup(group);
        session.setScheduledBy(scheduler);
        session.setScheduledAt(request.getScheduledAt());
        session.setLocation(request.getLocation());
        session.setNotes(request.getNotes());

        MeetingSession savedSession = meetingSessionRepository.save(session);

        notificationService.notifySessionScheduled(savedSession);

        return savedSession;
    }

    /** Returns all future sessions across every group the student belongs to, ordered by date. */
    public List<MeetingSession> getUpcomingSessions(String email) {
        Profile profile = currentProfile(email);
        return meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
                profile.getId(), LocalDateTime.now());
    }

    /** Returns all sessions for a specific group, ordered by scheduled time. Used on the group dashboard. */
    public List<MeetingSession> getSessionsForGroup(Long groupId) {
        return meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(groupId);
    }

    /** Deletes a session. Only the student who originally scheduled it can cancel. */
    public void cancelSession(Long sessionId, String requestingEmail) {
        Profile requester = currentProfile(requestingEmail);
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting session not found: " + sessionId));

        if (!session.getScheduledBy().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Only the session creator can cancel it");
        }

        meetingSessionRepository.delete(session);
    }

    // resolves the logged-in user's Profile from their email — throws 404 if not found
    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
