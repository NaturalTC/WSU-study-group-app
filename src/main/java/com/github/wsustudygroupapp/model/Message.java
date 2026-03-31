package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a single chat message sent inside a study group.
 * Messages are persisted to the database so chat history loads when a student opens a group.
 * New messages are broadcast in real time to all group members via WebSockets.
 */
@Entity
@Table(name = "message_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The text content of the message. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** The student who sent this message. */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Profile sender;

    /** The study group this message was sent in. */
    @ManyToOne
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    /** Timestamp of when the message was sent — set automatically by the server. */
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
