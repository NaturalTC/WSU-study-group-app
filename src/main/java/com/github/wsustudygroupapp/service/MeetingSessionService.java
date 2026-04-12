package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MeetingSessionRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.MeetingSession;
import com.github.wsustudygroupapp.repository.MeetingSessionRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
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
    private final NotificationService notificationService;

    public MeetingSessionService(MeetingSessionRepository meetingSessionRepository,
                                 StudyGroupRepository studyGroupRepository,
                                 ProfileRepository profileRepository,
                                 NotificationService notificationService) {
        this.meetingSessionRepository = meetingSessionRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
    }

    // TODO: find the group by request.getGroupId() — throw ResourceNotFoundException if missing
    // TODO: find the profile by schedulerProfileId — throw ResourceNotFoundException if missing
    // TODO: build a new MeetingSession with group, profile, scheduledAt, location, notes, createdAt
    // TODO: save the session
    // TODO: call notificationService.notifyGroupMembers() to notify all other group members
    // TODO: return the saved session
    public MeetingSession scheduleSession(Long schedulerProfileId, MeetingSessionRequest request) {
        return null;
    }

    // TODO: return meetingSessionRepository.findByStudyGroupMembersIdAndScheduledAtAfter(profileId, LocalDateTime.now())
    public List<MeetingSession> getUpcomingSessions(Long profileId) {
        return null;
    }

    // TODO: return meetingSessionRepository.findByStudyGroupIdOrderByScheduledAtAsc(groupId)
    public List<MeetingSession> getSessionsForGroup(Long groupId) {
        return null;
    }

    // TODO: find the session by sessionId — throw ResourceNotFoundException if missing
    // TODO: verify the requesting profile is the one who scheduled it — throw 403 if not
    // TODO: delete the session from the repository
    public void cancelSession(Long sessionId, Long requestingProfileId) {

    }
}
