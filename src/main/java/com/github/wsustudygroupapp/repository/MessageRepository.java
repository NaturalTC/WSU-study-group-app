package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the Message entity.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Returns all messages for a study group sorted oldest to newest. Used to load chat history. */
    List<Message> findByStudyGroupIdOrderBySentAtAsc(Long studyGroupId);

    /** Counts all messages sent by a specific student. Used by GamificationService for badge eligibility. */
    int countBySenderId(Long senderId);
}
