package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.StudyGroupRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final GamificationService gamificationService;
    private final S3Service s3Service;

    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             CourseRepository courseRepository,
                             ProfileRepository profileRepository,
                             UserRepository userRepository,
                             NotificationService notificationService,
                             PasswordEncoder passwordEncoder,
                             GamificationService gamificationService,
                             S3Service s3Service) {
        this.studyGroupRepository = studyGroupRepository;
        this.courseRepository = courseRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.gamificationService = gamificationService;
        this.s3Service = s3Service;
    }

    public StudyGroup getGroupById(Long groupId) {
        log.info("getGroupById called for groupId={}", groupId);
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));
    }

    public List<StudyGroup> getAllGroups() {
        log.info("getAllGroups called");
        return studyGroupRepository.findAll();
    }

    public List<StudyGroup> getGroupsForCourse(Long courseId) {
        log.info("getGroupsForCourse called for courseId={}", courseId);
        return studyGroupRepository.findByCourseId(courseId);
    }

    public List<StudyGroup> getMyGroups(String email) {
        log.info("getMyGroups called for email={}", email);
        Profile profile = currentProfile(email);
        return studyGroupRepository.findByMembersId(profile.getId());
    }

    public StudyGroup createGroup(StudyGroupRequest request, String creatorEmail) {
        log.info("createGroup called by email={} for courseId={}", creatorEmail, request.getCourseId());
        Profile creator = currentProfile(creatorEmail);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));

        StudyGroup group = new StudyGroup();
        group.setName(request.getName());
        group.setCourse(course);
        group.setCreatedBy(creator);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            group.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        List<Profile> members = new ArrayList<>();
        members.add(creator);
        group.setMembers(members);

        StudyGroup saved = studyGroupRepository.save(group);
        // Wrapped in try-catch so a gamification error never rolls back the group creation.
        try {
            gamificationService.awardPoints(creator.getId(), 15);
        } catch (Exception e) {
            log.error("Failed to award create-group points for profile {}: {}", creator.getId(), e.getMessage());
        }
        return saved;
    }

    public StudyGroup joinGroup(Long groupId, String password, String email) {
        log.info("joinGroup called by email={} for groupId={}", email, groupId);
        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(profile.getId()));
        if (alreadyMember) {
            log.warn("joinGroup rejected — email={} is already a member of groupId={}", email, groupId);
            return group;
        }

        if (group.getPassword() != null && !passwordEncoder.matches(password, group.getPassword())) {
            log.warn("joinGroup rejected — incorrect password for groupId={} by email={}", groupId, email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Incorrect group password");
        }

        group.getMembers().add(profile);
        StudyGroup saved = studyGroupRepository.save(group);
        notificationService.notifyMemberJoined(saved, profile);
        // Wrapped in try-catch so a gamification error never rolls back the group join.
        try {
            gamificationService.awardPoints(profile.getId(), 10);
        } catch (Exception e) {
            log.error("Failed to award join-group points for profile {}: {}", profile.getId(), e.getMessage());
        }
        return saved;
    }

    public void leaveGroup(Long groupId, String email) {
        log.info("leaveGroup called by email={} for groupId={}", email, groupId);
        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));

        group.getMembers().removeIf(member -> member.getId().equals(profile.getId()));
        studyGroupRepository.save(group);
    }

    public StudyGroup uploadGroupPicture(Long groupId, String email, MultipartFile file) throws java.io.IOException {
        log.info("uploadGroupPicture called by email={} for groupId={}", email, groupId);
        if (file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");
        if (!file.getContentType().startsWith("image/")) throw new IllegalArgumentException("File must be an image");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("File must be under 5MB");

        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));
        if (!group.getCreatedBy().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group creator can update the group picture");
        }

        String url = s3Service.uploadGroupPicture(file, groupId);
        group.setGroupPicURL(url);
        return studyGroupRepository.save(group);
    }

    public void deleteGroup(Long groupId, String email) {
        log.info("deleteGroup called by email={} for groupId={}", email, groupId);
        Profile profile = currentProfile(email);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found: " + groupId));
        if (!group.getCreatedBy().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group creator can delete this group");
        }
        studyGroupRepository.delete(group);
    }

    private Profile currentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
