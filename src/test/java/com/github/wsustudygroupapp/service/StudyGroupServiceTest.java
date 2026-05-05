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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private StudyGroupService studyGroupService;

    private static final String EMAIL = "student@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

    private User mockUser;
    private Profile mockProfile;
    private Course mockCourse;
    private StudyGroup openGroup;
    private StudyGroup protectedGroup;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setUser(mockUser);

        mockCourse = new Course();
        mockCourse.setId(10L);

        openGroup = new StudyGroup();
        openGroup.setId(1L);
        openGroup.setName("Open Study Group");
        openGroup.setCourse(mockCourse);
        openGroup.setCreatedBy(mockProfile);
        openGroup.setPassword(null);
        openGroup.setMembers(new ArrayList<>());

        protectedGroup = new StudyGroup();
        protectedGroup.setId(2L);
        protectedGroup.setName("Protected Study Group");
        protectedGroup.setCourse(mockCourse);
        protectedGroup.setCreatedBy(mockProfile);
        protectedGroup.setPassword("$2a$hashed");
        protectedGroup.setMembers(new ArrayList<>());
    }

    private void stubResolveProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
    }

    // ── createGroup ───────────────────────────────────────────────────────────

    @Test
    void createGroup_withPassword_savesHashedPassword() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(passwordEncoder.encode("secret")).thenReturn("$2a$hashed");
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StudyGroupRequest request = new StudyGroupRequest();
        request.setName("CS Study Group");
        request.setCourseId(10L);
        request.setPassword("secret");

        StudyGroup result = studyGroupService.createGroup(request, EMAIL);

        assertEquals("$2a$hashed", result.getPassword());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void createGroup_withNullPassword_savesNullAndNeverHashes() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StudyGroupRequest request = new StudyGroupRequest();
        request.setName("Open Study Group");
        request.setCourseId(10L);
        request.setPassword(null);

        StudyGroup result = studyGroupService.createGroup(request, EMAIL);

        assertNull(result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createGroup_withBlankPassword_savesNullAndNeverHashes() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StudyGroupRequest request = new StudyGroupRequest();
        request.setName("Open Study Group");
        request.setCourseId(10L);
        request.setPassword("   ");

        StudyGroup result = studyGroupService.createGroup(request, EMAIL);

        assertNull(result.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void createGroup_creatorIsAddedAsOnlyMember() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(mockCourse));
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StudyGroupRequest request = new StudyGroupRequest();
        request.setName("CS Study Group");
        request.setCourseId(10L);

        StudyGroup result = studyGroupService.createGroup(request, EMAIL);

        assertEquals(1, result.getMembers().size());
        assertTrue(result.getMembers().contains(mockProfile));
    }

    // ── joinGroup — open group ─────────────────────────────────────────────────

    @Test
    void joinGroup_openGroup_joinsWithoutPasswordCheck() {
        stubResolveProfile();
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(openGroup));
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studyGroupService.joinGroup(1L, null, EMAIL);

        verify(passwordEncoder, never()).matches(any(), any());
        assertTrue(openGroup.getMembers().contains(mockProfile));
    }

    @Test
    void joinGroup_openGroup_sendsJoinedNotification() {
        stubResolveProfile();
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(openGroup));
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studyGroupService.joinGroup(1L, null, EMAIL);

        verify(notificationService).notifyMemberJoined(any(StudyGroup.class), eq(mockProfile));
    }

    // ── joinGroup — password-protected group ───────────────────────────────────

    @Test
    void joinGroup_correctPassword_addsUserToGroup() {
        stubResolveProfile();
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));
        when(passwordEncoder.matches("secret", "$2a$hashed")).thenReturn(true);
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studyGroupService.joinGroup(2L, "secret", EMAIL);

        assertTrue(protectedGroup.getMembers().contains(mockProfile));
    }

    @Test
    void joinGroup_correctPassword_sendsJoinedNotification() {
        stubResolveProfile();
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));
        when(passwordEncoder.matches("secret", "$2a$hashed")).thenReturn(true);
        when(studyGroupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studyGroupService.joinGroup(2L, "secret", EMAIL);

        verify(notificationService).notifyMemberJoined(any(StudyGroup.class), eq(mockProfile));
    }

    @Test
    void joinGroup_wrongPassword_throws403Forbidden() {
        stubResolveProfile();
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> studyGroupService.joinGroup(2L, "wrong", EMAIL));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void joinGroup_wrongPassword_doesNotAddUserOrSave() {
        stubResolveProfile();
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> studyGroupService.joinGroup(2L, "wrong", EMAIL));

        assertFalse(protectedGroup.getMembers().contains(mockProfile));
        verify(studyGroupRepository, never()).save(any());
    }

    @Test
    void joinGroup_wrongPassword_doesNotSendNotification() {
        stubResolveProfile();
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> studyGroupService.joinGroup(2L, "wrong", EMAIL));

        verify(notificationService, never()).notifyMemberJoined(any(), any());
    }

    // ── joinGroup — already a member ───────────────────────────────────────────

    @Test
    void joinGroup_alreadyMember_returnsGroupWithoutSaving() {
        stubResolveProfile();
        openGroup.getMembers().add(mockProfile);
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(openGroup));

        StudyGroup result = studyGroupService.joinGroup(1L, null, EMAIL);

        assertEquals(openGroup, result);
        verify(studyGroupRepository, never()).save(any());
        verify(notificationService, never()).notifyMemberJoined(any(), any());
    }

    @Test
    void joinGroup_alreadyMemberOfProtectedGroup_doesNotCheckPassword() {
        // password check should be skipped entirely — member status is checked first
        stubResolveProfile();
        protectedGroup.getMembers().add(mockProfile);
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(protectedGroup));

        studyGroupService.joinGroup(2L, "anything", EMAIL);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ── joinGroup — error cases ────────────────────────────────────────────────

    @Test
    void joinGroup_groupNotFound_throwsResourceNotFoundException() {
        stubResolveProfile();
        when(studyGroupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studyGroupService.joinGroup(99L, null, EMAIL));
    }

    @Test
    void joinGroup_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studyGroupService.joinGroup(1L, null, UNKNOWN_EMAIL));
    }
}
