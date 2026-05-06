package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             CourseRepository courseRepository,
                             ProfileRepository profileRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.studyGroupRepository = studyGroupRepository;
        this.courseRepository = courseRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public StudyGroup getGroupById(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));
    }

    public List<StudyGroup> getAllGroups() {
        return studyGroupRepository.findAll();
    }

    public List<StudyGroup> getGroupsForCourse(Long courseId) {
        return studyGroupRepository.findByCourseId(courseId);
    }

    public List<StudyGroup> getMyGroups(String email) {
        Profile profile = currentProfile(email);
        return studyGroupRepository.findByMembersId(profile.getId());
    }

    public StudyGroup createGroup(StudyGroupRequest request, String creatorEmail) {
        Profile creator = currentProfile(creatorEmail);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));

        StudyGroup group = new StudyGroup();
        group.setName(request.getName());
        group.setCourse(course);
        group.setCreatedBy(creator);

        List<Profile> members = new ArrayList<>();
        members.add(creator);
        group.setMembers(members);

        return studyGroupRepository.save(group);
    }

    public StudyGroup joinGroup(Long groupId, String email) {
        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(profile.getId()));
        if (!alreadyMember) {
            group.getMembers().add(profile);
            StudyGroup saved = studyGroupRepository.save(group);
            notificationService.notifyMemberJoined(saved, profile);
            return saved;
        }

        return group;
    }

    public void leaveGroup(Long groupId, String email) {
        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));

        group.getMembers().removeIf(member -> member.getId().equals(profile.getId()));
        studyGroupRepository.save(group);
    }

    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
