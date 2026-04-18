package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a scheduled study session for a study group.
 * Any group member can create a session — all members are notified automatically.
 * Sessions have an optional location and notes visible to all members.
 */
@Entity
@Table(name = "meeting_session_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSession {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The study group this session belongs to. */
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup;

    /** The profile of the student who scheduled this session. */
    @ManyToOne
    @JoinColumn(name = "scheduled_by", nullable = false)
    private Profile scheduledBy;

    /** Date and time the session is set to start. */
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    /** Where the session will take place (e.g. "Ely Library Rm 204" or "Zoom"). */
    @Column
    private String location;

    /** Optional notes or agenda for the session (e.g. "Reviewing chapters 5–7"). */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** When this record was created. Set automatically on insert. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
