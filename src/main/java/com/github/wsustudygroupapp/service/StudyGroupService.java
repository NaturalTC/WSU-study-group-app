package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Hayden — handles study group creation, joining, and leaving
// getGroupsForCourse() → return all groups for a given course
// createGroup()        → create a new study group for a course
// joinGroup()          → add a student to an existing group
// leaveGroup()         → remove a student from a group

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
    // TODO: build a new StudyGroup with name, course, createdBy = currentProfile(email)
    // TODO: add creator as first member, save and return
    public StudyGroup createGroup(StudyGroupRequest request, String email) {
        Profile creator = currentProfile(email);
        return null;
    }

    // TODO: find the group by groupId — throw if not found
    // TODO: check the student isn't already a member
    // TODO: add currentProfile(email) to group.getMembers() and save
    public StudyGroup joinGroup(Long groupId, String email) {
        Profile profile = currentProfile(email);
        return null;
    }

    // TODO: find the group by groupId — throw if not found
    // TODO: remove currentProfile(email) from group.getMembers() and save
    public void leaveGroup(Long groupId, String email) {
        Profile profile = currentProfile(email);
    }

    // Looks up the Profile for the logged-in user using their email from the JWT.
    // Call this at the top of any method that needs to know who is making the request.
    private Profile currentProfile(String email) {
        return profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found for email: " + email));
    }
}
