package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.MessageRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.service.ActiveDmTracker;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private GamificationService gamificationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActiveDmTracker activeDmTracker;

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

    @Test
    void saveMessage_groupMessage_doesNotTriggerDmNotification() {
        MessageDTO dto = new MessageDTO();
        dto.setStudyGroupId(42L);
        dto.setSenderName("Brian");
        dto.setContent("Hello!");
        dto.setSentAt(LocalDateTime.now());

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(studyGroupRepository.getReferenceById(42L)).thenReturn(testGroup);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        chatService.saveMessage(dto);

        verify(notificationService, never()).notifyDirectMessage(any(), any());
    }

    // ── DM saveMessage ────────────────────────────────────────────────────────

    @Test
    void saveMessage_dm_setsDmRoomIdOnMessage() {
        Profile recipient = new Profile();
        recipient.setId(2L);

        MessageDTO dto = new MessageDTO();
        dto.setSenderName("Brian");
        dto.setContent("Hey!");
        dto.setSentAt(LocalDateTime.now());
        dto.setDmRoomId("dm-1-2");

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message result = chatService.saveMessage(dto);

        assertEquals("dm-1-2", result.getDmRoomId());
    }

    @Test
    void saveMessage_dm_doesNotSetStudyGroup() {
        Profile recipient = new Profile();
        recipient.setId(2L);

        MessageDTO dto = new MessageDTO();
        dto.setSenderName("Brian");
        dto.setContent("Hey!");
        dto.setSentAt(LocalDateTime.now());
        dto.setDmRoomId("dm-1-2");

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message result = chatService.saveMessage(dto);

        assertNull(result.getStudyGroup());
        verify(studyGroupRepository, never()).getReferenceById(any());
    }

    @Test
    void saveMessage_dm_notifiesRecipient() {
        Profile recipient = new Profile();
        recipient.setId(2L);

        MessageDTO dto = new MessageDTO();
        dto.setSenderName("Brian");
        dto.setContent("Hey!");
        dto.setSentAt(LocalDateTime.now());
        dto.setDmRoomId("dm-1-2");

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        chatService.saveMessage(dto);

        verify(notificationService).notifyDirectMessage(recipient, testProfile);
    }

    @Test
    void saveMessage_dm_recipientNotFound_stillSavesMessage() {
        MessageDTO dto = new MessageDTO();
        dto.setSenderName("Brian");
        dto.setContent("Hey!");
        dto.setSentAt(LocalDateTime.now());
        dto.setDmRoomId("dm-1-99");

        when(profileRepository.findByName("Brian")).thenReturn(Optional.of(testProfile));
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        assertDoesNotThrow(() -> chatService.saveMessage(dto));
        verify(messageRepository).save(any(Message.class));
        verify(notificationService, never()).notifyDirectMessage(any(), any());
    }

    // ── getDmHistory ──────────────────────────────────────────────────────────

    @Test
    void getDmHistory_returnsMessagesFromRepo() {
        when(messageRepository.findByDmRoomIdOrderBySentAtAsc("dm-1-2"))
                .thenReturn(List.of(testMessage));

        List<Message> result = chatService.getDmHistory("dm-1-2");

        assertEquals(1, result.size());
        verify(messageRepository).findByDmRoomIdOrderBySentAtAsc("dm-1-2");
    }

    @Test
    void getDmHistory_noMessages_returnsEmptyList() {
        when(messageRepository.findByDmRoomIdOrderBySentAtAsc("dm-1-2"))
                .thenReturn(List.of());

        assertTrue(chatService.getDmHistory("dm-1-2").isEmpty());
    }
}
