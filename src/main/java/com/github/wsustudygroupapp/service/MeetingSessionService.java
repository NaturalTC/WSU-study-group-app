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

// TODO: Hayden Parker — handles scheduling, fetching, and cancelling group study sessions
// scheduleSession()    → create a new session and notify all group members
// getUpcomingSessions() → return all future sessions for a student across all their groups
// getSessionsForGroup() → return all sessions for a specific group
// cancelSession()      → delete a session (only the creator can cancel)

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
        session.setCreatedAt(LocalDateTime.now());

        MeetingSession savedSession = meetingSessionRepository.save(session);

        String message = "New study session scheduled: " + request.getScheduledAt() + " at " + request.getLocation();
        notificationService.notifyGroupMembers(group, message, scheduler.getId());

        return savedSession;
    }

    public List<MeetingSession> getUpcomingSessions(String email) {
        Profile profile = currentProfile(email);
        return meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
                profile.getId(), LocalDateTime.now());
    }

    public List<MeetingSession> getSessionsForGroup(Long groupId) {
        return meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(groupId);
    }

    public void cancelSession(Long sessionId, String requestingEmail) {
        Profile requester = currentProfile(requestingEmail);
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting session not found: " + sessionId));

        if (!session.getScheduledBy().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Only the session creator can cancel it");
        }

        meetingSessionRepository.delete(session);
    }

    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
