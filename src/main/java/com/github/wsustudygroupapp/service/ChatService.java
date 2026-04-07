package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.MessageRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// TODO: Brian — handles saving messages and loading chat history
// getHistory() → load all past messages for a group when a student opens the chat
// saveMessage() → persist an incoming WebSocket message to the database

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;

    public ChatService(MessageRepository messageRepository,
                       StudyGroupRepository studyGroupRepository,
                       ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.profileRepository = profileRepository;
    }

    // TODO: return messageRepository.findByStudyGroupIdOrderBySentAtAsc(groupId)
    public List<Message> getHistory(Long groupId)
    {
        return messageRepository.findByStudyGroupIdOrderBySentAtAsc(groupId);
    }

    // TODO: find the StudyGroup by dto.getStudyGroupId()
    // TODO: find the Profile by looking up sender by name or ID
    // TODO: build a new Message with content, sender, studyGroup, sentAt = now
    // TODO: save the message to the database
    // TODO: return the saved message
    public Message saveMessage(MessageDTO dto)
    {
        Long groupId = dto.getStudyGroupId();
        String name = dto.getSenderName();
        Profile sender = profileRepository.findByName(name).orElseThrow
                (
                    () -> new RuntimeException("Could not find profile")
                );
        StudyGroup studyGroup = studyGroupRepository.getReferenceById(groupId);
        String content = dto.getContent();
        Message savedMessage = new Message();
        savedMessage.setStudyGroup(studyGroup);
        savedMessage.setSender(sender);
        savedMessage.setContent(content);
        savedMessage.setSentAt(dto.getSentAt());
        messageRepository.save(savedMessage);
        return savedMessage;
    }
}
