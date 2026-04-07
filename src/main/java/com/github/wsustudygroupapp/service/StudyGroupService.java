package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Hayden — handles study group creation, joining, and leaving
// getGroupsForCourse() → return all groups for a given course
// createGroup() → create a new study group for a course
// joinGroup() → add a student to an existing group
// leaveGroup() → remove a student from a group

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final ProfileRepository profileRepository;

    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             CourseRepository courseRepository,
                             ProfileRepository profileRepository) {
        this.studyGroupRepository = studyGroupRepository;
        this.courseRepository = courseRepository;
        this.profileRepository = profileRepository;
    }

    // TODO: return studyGroupRepository.findByCourseId(courseId)
    public List<StudyGroup> getGroupsForCourse(Long courseId) {
        return null;
    }

    // TODO: find the course by request.getCourseId()
    // TODO: find the profile by creatorProfileId
    // TODO: build a new StudyGroup with name, course, createdBy, and add creator as first member
    // TODO: save and return the group
    public StudyGroup createGroup(Long creatorProfileId, StudyGroupRequest request) {
        return null;
    }

    // TODO: find the group by groupId — throw exception if not found
    // TODO: find the profile by profileId
    // TODO: check the student isn't already a member
    // TODO: add the profile to group.getMembers() and save
    public StudyGroup joinGroup(Long groupId, Long profileId) {
        return null;
    }

    // TODO: find the group by groupId — throw exception if not found
    // TODO: remove the profile from group.getMembers() and save
    public void leaveGroup(Long groupId, Long profileId) {

    }
}
