package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.CourseStudentResponse;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.Friendship;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.FriendshipRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserCourseRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public CourseService(CourseRepository courseRepository,
                         UserCourseRepository userCourseRepository,
                         ProfileRepository profileRepository,
                         UserRepository userRepository,
                         FriendshipRepository friendshipRepository) {
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public List<Course> getAllCourses() {
        log.info("getAllCourses called");
        return courseRepository.findAll();
    }

    /**
     * Returns all students enrolled in a course, optionally filtered to a single section.
     * Annotates each result with the current user's friendship status toward that student.
     */
    public List<CourseStudentResponse> getCourseStudents(Long courseId, String section, String email) {
        log.info("getCourseStudents called for courseId={}, section={}, email={}", courseId, section, email);
        Profile me = resolveProfile(email);

        List<UserCourse> enrollments = (section != null && !section.isBlank())
                ? userCourseRepository.findByCourseAndSectionExcluding(courseId, section, me.getId())
                : userCourseRepository.findAllByCourseExcluding(courseId, me.getId());

        Map<Long, Friendship> friendshipMap = friendshipRepository.findAllInvolving(me.getId())
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getSender().getId().equals(me.getId())
                                ? f.getReceiver().getId()
                                : f.getSender().getId(),
                        f -> f,
                        (a, b) -> a
                ));

        return enrollments.stream().map(uc -> {
            Profile p = uc.getProfile();
            Optional<Friendship> rel = Optional.ofNullable(friendshipMap.get(p.getId()));

            Friendship.FriendshipStatus status = rel.map(Friendship::getStatus).orElse(null);
            String direction = rel.map(f ->
                    f.getSender().getId().equals(me.getId()) ? "SENT" : "RECEIVED"
            ).orElse(null);
            Long friendshipId = rel.map(Friendship::getId).orElse(null);

            return new CourseStudentResponse(
                    p.getId(), p.getName(), p.getMajor(), p.getYear(),
                    uc.getSection(), uc.getSemester(),
                    status, direction, friendshipId
            );
        }).toList();
    }

    private Profile resolveProfile(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
