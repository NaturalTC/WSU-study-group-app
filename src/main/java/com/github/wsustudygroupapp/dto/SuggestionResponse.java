package com.github.wsustudygroupapp.dto;

/** A suggested friend — someone in a shared study group not yet connected to the current user. */
public class SuggestionResponse {

    private Long   profileId;
    private String name;
    private String major;
    private String year;
    private String bio;
    private String sharedGroupName;

    public SuggestionResponse() {}

    public SuggestionResponse(Long profileId, String name, String major,
                               String year, String bio, String sharedGroupName) {
        this.profileId       = profileId;
        this.name            = name;
        this.major           = major;
        this.year            = year;
        this.bio             = bio;
        this.sharedGroupName = sharedGroupName;
    }

    public Long   getProfileId()       { return profileId; }
    public String getName()            { return name; }
    public String getMajor()           { return major; }
    public String getYear()            { return year; }
    public String getBio()             { return bio; }
    public String getSharedGroupName() { return sharedGroupName; }
}
