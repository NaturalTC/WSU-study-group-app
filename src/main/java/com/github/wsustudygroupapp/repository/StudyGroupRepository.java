package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the StudyGroup entity.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    /** Returns all study groups tied to a specific course. Used to display groups a student can join. */
    List<StudyGroup> findByCourseId(Long courseId);

    /** Returns all study groups a student is a member of. Used to display "My Groups". */
    List<StudyGroup> findByMembersId(Long profileId);

    /** Returns all study groups created by a specific student. */
    List<StudyGroup> findByCreatedById(Long profileId);
}
