package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.MessageRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ChatService chatService;

    private Profile testProfile;
    private StudyGroup testGroup;
    private Message testMessage;

    @BeforeEach
    void setup() {
        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setName("Brian");

        testGroup = new StudyGroup();
        testGroup.setId(42L);

        testMessage = new Message();
        testMessage.setContent("Hello!");
        testMessage.setSender(testProfile);
        testMessage.setStudyGroup(testGroup);
        testMessage.setSentAt(LocalDateTime.now());
    }

    @Test
    void getHistory_returnsMessagesInOrder() {
        when(messageRepository.findByStudyGroupIdOrderBySentAtAsc(42L))
                .thenReturn(List.of(testMessage));

        List<Message> result = chatService.getHistory(42L);

        assertEquals(1, result.size());
        assertEquals("Hello!", result.get(0).getContent());
        verify(messageRepository).findByStudyGroupIdOrderBySentAtAsc(42L);
    }

    @Test
    void getHistory_emptyGroup_returnsEmptyList() {
        when(messageRepository.findByStudyGroupIdOrderBySentAtAsc(42L))
                .thenReturn(List.of());

        List<Message> result = chatService.getHistory(42L);

        assertTrue(result.isEmpty());
    }

    @Test
    void saveMessage_savesAndReturnsMessage() {
        MessageDTO dto = new MessageDTO();
        dto.setStudyGroupId(42L);
        dto.setSenderName("Brian");
        dto.setContent("Hello!");
        dto.setSentAt(LocalDateTime.now());

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(studyGroupRepository.getReferenceById(42L)).thenReturn(testGroup);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = chatService.saveMessage(dto);

        assertNotNull(result);
        assertEquals("Hello!", result.getContent());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void saveMessage_senderNotFound_throwsException() {
        MessageDTO dto = new MessageDTO();
        dto.setStudyGroupId(42L);
        dto.setSenderName("Unknown");
        dto.setContent("Hello!");

        when(profileRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> chatService.saveMessage(dto));
        verify(messageRepository, never()).save(any());
    }
}
