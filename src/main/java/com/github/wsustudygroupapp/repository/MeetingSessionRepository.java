package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.MeetingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Database operations for the MeetingSession entity.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface MeetingSessionRepository extends JpaRepository<MeetingSession, Long> {

    /** Returns all sessions for a given group ordered by time. Used on the group dashboard. */
    List<MeetingSession> findByStudyGroupIdOrderByScheduledAtAsc(Long groupId);

    /** Returns all upcoming sessions for a student across all their groups. */
    List<MeetingSession> findByStudyGroupMembersIdAndScheduledAtAfterOrderByScheduledAtAsc(
        Long profileId, LocalDateTime now
    );

    /** Returns sessions for a group within a date range. Used for calendar view. */
    List<MeetingSession> findByStudyGroupIdAndScheduledAtBetweenOrderByScheduledAtAsc(
        Long groupId, LocalDateTime start, LocalDateTime end
    );
}
