package com.github.wsustudygroupapp.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which users currently have a DM conversation open.
 * Used to suppress notifications when the recipient is already viewing the chat.
 * Keyed by STOMP session ID so entries are cleaned up automatically on disconnect.
 */
@Component
public class ActiveDmTracker {

    private final Map<String, String> sessionToKey = new ConcurrentHashMap<>();
    private final Set<String> activeKeys = ConcurrentHashMap.newKeySet();

    public void enter(String sessionId, Long profileId, String dmRoomId) {
        leave(sessionId); // clear any prior room registered for this session
        String key = profileId + ":" + dmRoomId;
        sessionToKey.put(sessionId, key);
        activeKeys.add(key);
    }

    public void leave(String sessionId) {
        String key = sessionToKey.remove(sessionId);
        if (key != null) activeKeys.remove(key);
    }

    public boolean isActive(Long profileId, String dmRoomId) {
        return activeKeys.contains(profileId + ":" + dmRoomId);
    }
}
