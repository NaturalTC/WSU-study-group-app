package com.github.wsustudygroupapp.dto;

import com.github.wsustudygroupapp.model.Friendship.FriendshipStatus;

import java.time.LocalDateTime;

/** Returned by /friends endpoints — carries the other person's info plus request state. */
public class FriendshipResponse {

    private Long friendshipId;
    private Long profileId;
    private String name;
    private String major;
    private String year;
    private String bio;
    private FriendshipStatus status;
    /** SENT if the current user sent this request; RECEIVED if they received it. */
    private String direction;
    private LocalDateTime createdAt;

    public FriendshipResponse() {}

    public FriendshipResponse(Long friendshipId, Long profileId, String name, String major,
                               String year, String bio, FriendshipStatus status,
                               String direction, LocalDateTime createdAt) {
        this.friendshipId = friendshipId;
        this.profileId    = profileId;
        this.name         = name;
        this.major        = major;
        this.year         = year;
        this.bio          = bio;
        this.status       = status;
        this.direction    = direction;
        this.createdAt    = createdAt;
    }

    public Long getFriendshipId() { return friendshipId; }
    public Long getProfileId()    { return profileId; }
    public String getName()       { return name; }
    public String getMajor()      { return major; }
    public String getYear()       { return year; }
    public String getBio()        { return bio; }
    public FriendshipStatus getStatus() { return status; }
    public String getDirection()  { return direction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
