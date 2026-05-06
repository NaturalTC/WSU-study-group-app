package com.github.wsustudygroupapp.dto;

import com.github.wsustudygroupapp.model.Friendship.FriendshipStatus;

/** One student returned by GET /courses/{courseId}/students. */
public class CourseStudentResponse {

    private Long profileId;
    private String name;
    private String major;
    private String year;
    private String section;
    private String semester;
    /** null = not connected, PENDING = request pending, ACCEPTED = already friends. */
    private FriendshipStatus friendshipStatus;
    /** SENT or RECEIVED when friendshipStatus is PENDING; null otherwise. */
    private String friendshipDirection;
    private Long friendshipId;

    public CourseStudentResponse() {}

    public CourseStudentResponse(Long profileId, String name, String major, String year,
                                  String section, String semester,
                                  FriendshipStatus friendshipStatus, String friendshipDirection,
                                  Long friendshipId) {
        this.profileId           = profileId;
        this.name                = name;
        this.major               = major;
        this.year                = year;
        this.section             = section;
        this.semester            = semester;
        this.friendshipStatus    = friendshipStatus;
        this.friendshipDirection = friendshipDirection;
        this.friendshipId        = friendshipId;
    }

    public Long getProfileId()                { return profileId; }
    public String getName()                   { return name; }
    public String getMajor()                  { return major; }
    public String getYear()                   { return year; }
    public String getSection()                { return section; }
    public String getSemester()               { return semester; }
    public FriendshipStatus getFriendshipStatus()   { return friendshipStatus; }
    public String getFriendshipDirection()    { return friendshipDirection; }
    public Long getFriendshipId()             { return friendshipId; }
}
