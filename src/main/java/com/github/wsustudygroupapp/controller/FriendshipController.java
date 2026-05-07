package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.FriendshipResponse;
import com.github.wsustudygroupapp.dto.SuggestionResponse;
import com.github.wsustudygroupapp.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friendships", description = "Send, accept, decline, and manage friend connections between students")
@RestController
@RequestMapping("/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    /** GET /friends — accepted friends list */
    @Operation(summary = "Get the logged-in student's accepted friends list")
    @GetMapping
    public ResponseEntity<List<FriendshipResponse>> getFriends(Authentication auth) {
        return ResponseEntity.ok(friendshipService.getFriends(auth.getName()));
    }

    /** GET /friends/requests/incoming — pending requests received by me */
    @Operation(summary = "Get pending friend requests received by the logged-in student")
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendshipResponse>> getIncoming(Authentication auth) {
        return ResponseEntity.ok(friendshipService.getIncomingRequests(auth.getName()));
    }

    /** GET /friends/requests/outgoing — pending requests I sent */
    @Operation(summary = "Get pending friend requests sent by the logged-in student")
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendshipResponse>> getOutgoing(Authentication auth) {
        return ResponseEntity.ok(friendshipService.getOutgoingRequests(auth.getName()));
    }

    /** GET /friends/suggestions — group-chat members not yet connected */
    @Operation(summary = "Get suggested connections from shared study groups")
    @GetMapping("/suggestions")
    public ResponseEntity<List<SuggestionResponse>> getSuggestions(Authentication auth) {
        return ResponseEntity.ok(friendshipService.getSuggestions(auth.getName()));
    }

    /** GET /friends/search?q=name — search all profiles by name */
    @Operation(summary = "Search for students by name")
    @GetMapping("/search")
    public ResponseEntity<List<SuggestionResponse>> search(Authentication auth,
                                                            @RequestParam String q) {
        return ResponseEntity.ok(friendshipService.searchProfiles(q, auth.getName()));
    }

    /** POST /friends/request/{profileId} — send a friend request */
    @Operation(summary = "Send a friend request to another student")
    @PostMapping("/request/{profileId}")
    public ResponseEntity<FriendshipResponse> sendRequest(Authentication auth,
                                                           @PathVariable Long profileId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendRequest(auth.getName(), profileId));
    }

    /** PATCH /friends/{friendshipId}/accept — accept a received request */
    @Operation(summary = "Accept a received friend request")
    @PatchMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendshipResponse> accept(Authentication auth,
                                                      @PathVariable Long friendshipId) {
        return ResponseEntity.ok(friendshipService.acceptRequest(auth.getName(), friendshipId));
    }

    /** PATCH /friends/{friendshipId}/decline — decline a received request */
    @Operation(summary = "Decline a received friend request")
    @PatchMapping("/{friendshipId}/decline")
    public ResponseEntity<FriendshipResponse> decline(Authentication auth,
                                                       @PathVariable Long friendshipId) {
        return ResponseEntity.ok(friendshipService.declineRequest(auth.getName(), friendshipId));
    }

    /** DELETE /friends/{friendshipId} — remove friend or cancel request */
    @Operation(summary = "Remove a friend or cancel a pending request")
    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> remove(Authentication auth,
                                       @PathVariable Long friendshipId) {
        friendshipService.removeFriendOrCancel(auth.getName(), friendshipId);
        return ResponseEntity.noContent().build();
    }
}
