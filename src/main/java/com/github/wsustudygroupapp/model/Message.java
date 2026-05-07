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

    /** The student who sent this message. Null for system/AI messages — use senderName instead. */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = true)
    private Profile sender;

    /** Display name used when sender is null (e.g. "AI Assistant"). */
    @Column(name = "sender_name")
    private String senderName;

    /** The study group this message was sent in. Null for direct messages. */
    @ManyToOne
    @JoinColumn(name = "study_group_id", nullable = true)
    private StudyGroup studyGroup;

    /** Canonical DM room ID (e.g. "dm-1-5"). Null for group messages. */
    @Column(name = "dm_room_id")
    private String dmRoomId;

    /** Timestamp of when the message was sent — set automatically by the server. */
    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
