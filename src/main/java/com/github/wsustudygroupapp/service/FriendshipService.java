package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.FriendshipResponse;
import com.github.wsustudygroupapp.dto.SuggestionResponse;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Friendship;
import com.github.wsustudygroupapp.model.Friendship.FriendshipStatus;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.StudyGroup;
import com.github.wsustudygroupapp.repository.FriendshipRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendshipRepository  friendshipRepository;
    private final ProfileRepository     profileRepository;
    private final UserRepository        userRepository;
    private final StudyGroupRepository  studyGroupRepository;

    public FriendshipService(FriendshipRepository friendshipRepository,
                              ProfileRepository profileRepository,
                              UserRepository userRepository,
                              StudyGroupRepository studyGroupRepository) {
        this.friendshipRepository = friendshipRepository;
        this.profileRepository    = profileRepository;
        this.userRepository       = userRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    /** Send a friend request from the logged-in user to targetProfileId. */
    public FriendshipResponse sendRequest(String email, Long targetProfileId) {
        Profile sender   = resolveProfile(email);
        Profile receiver = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + targetProfileId));

        if (sender.getId().equals(receiver.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add yourself as a friend");
        }

        friendshipRepository.findBetween(sender.getId(), receiver.getId()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A friend request already exists between these users");
        });

        Friendship friendship = new Friendship();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(friendship);

        return toResponse(saved, sender.getId());
    }

    /** Accept an incoming friend request (caller must be the receiver). */
    public FriendshipResponse acceptRequest(String email, Long friendshipId) {
        Profile me         = resolveProfile(email);
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        if (!friendship.getReceiver().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can accept this request");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return toResponse(friendshipRepository.save(friendship), me.getId());
    }

    /** Decline an incoming friend request (caller must be the receiver). */
    public FriendshipResponse declineRequest(String email, Long friendshipId) {
        Profile me         = resolveProfile(email);
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        if (!friendship.getReceiver().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can decline this request");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not pending");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        return toResponse(friendshipRepository.save(friendship), me.getId());
    }

    /**
     * Remove an accepted friendship or cancel a sent/received pending request.
     * Either party may do this.
     */
    public void removeFriendOrCancel(String email, Long friendshipId) {
        Profile me         = resolveProfile(email);
        Friendship friendship = getFriendshipOrThrow(friendshipId);

        boolean involved = friendship.getSender().getId().equals(me.getId())
                        || friendship.getReceiver().getId().equals(me.getId());
        if (!involved) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this friendship");
        }

        friendshipRepository.delete(friendship);
    }

    /** All accepted friends for the logged-in user. */
    public List<FriendshipResponse> getFriends(String email) {
        Profile me = resolveProfile(email);
        return friendshipRepository.findAcceptedFriends(me.getId())
                .stream()
                .map(f -> toResponse(f, me.getId()))
                .toList();
    }

    /** Pending requests received by the logged-in user. */
    public List<FriendshipResponse> getIncomingRequests(String email) {
        Profile me = resolveProfile(email);
        return friendshipRepository.findByReceiverIdAndStatus(me.getId(), FriendshipStatus.PENDING)
                .stream()
                .map(f -> toResponse(f, me.getId()))
                .toList();
    }

    /** Pending requests sent by the logged-in user. */
    public List<FriendshipResponse> getOutgoingRequests(String email) {
        Profile me = resolveProfile(email);
        return friendshipRepository.findBySenderIdAndStatus(me.getId(), FriendshipStatus.PENDING)
                .stream()
                .map(f -> toResponse(f, me.getId()))
                .toList();
    }

    /**
     * People in shared study groups who aren't already connected to the current user.
     * Each suggestion carries the name of one shared group for context.
     */
    public List<SuggestionResponse> getSuggestions(String email) {
        Profile me = resolveProfile(email);

        // IDs of everyone already connected (any status, either direction)
        Set<Long> alreadyConnected = friendshipRepository.findAllInvolving(me.getId())
                .stream()
                .map(f -> f.getSender().getId().equals(me.getId())
                        ? f.getReceiver().getId()
                        : f.getSender().getId())
                .collect(Collectors.toSet());
        alreadyConnected.add(me.getId());

        // Walk every group the user is in; collect members not already connected
        // Use a LinkedHashMap keyed by profileId so each person appears only once (first group wins)
        Map<Long, SuggestionResponse> seen = new LinkedHashMap<>();
        for (StudyGroup group : studyGroupRepository.findByMembersId(me.getId())) {
            for (Profile member : group.getMembers()) {
                if (!alreadyConnected.contains(member.getId()) && !seen.containsKey(member.getId())) {
                    seen.put(member.getId(), new SuggestionResponse(
                            member.getId(),
                            member.getName(),
                            member.getMajor(),
                            member.getYear(),
                            member.getBio(),
                            group.getName()
                    ));
                }
            }
        }
        return new ArrayList<>(seen.values());
    }

    /** Case-insensitive name search across all profiles, excluding the current user. */
    public List<SuggestionResponse> searchProfiles(String query, String email) {
        Profile me = resolveProfile(email);

        // Build existing-connection map for status overlay
        Map<Long, Friendship> friendshipMap = friendshipRepository.findAllInvolving(me.getId())
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getSender().getId().equals(me.getId())
                                ? f.getReceiver().getId()
                                : f.getSender().getId(),
                        f -> f,
                        (a, b) -> a
                ));

        return profileRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .filter(p -> !p.getId().equals(me.getId()))
                .map(p -> new SuggestionResponse(
                        p.getId(),
                        p.getName(),
                        p.getMajor(),
                        p.getYear(),
                        p.getBio(),
                        null  // no shared-group context for search results
                ))
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private FriendshipResponse toResponse(Friendship f, Long myProfileId) {
        boolean iSentIt   = f.getSender().getId().equals(myProfileId);
        Profile other     = iSentIt ? f.getReceiver() : f.getSender();
        String  direction = iSentIt ? "SENT" : "RECEIVED";

        return new FriendshipResponse(
                f.getId(),
                other.getId(),
                other.getName(),
                other.getMajor(),
                other.getYear(),
                other.getBio(),
                f.getStatus(),
                direction,
                f.getCreatedAt()
        );
    }

    private Friendship getFriendshipOrThrow(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found: " + id));
    }

    private Profile resolveProfile(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
