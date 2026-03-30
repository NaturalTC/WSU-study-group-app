package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
    Profile entity stores app-specific user data (major, year, bio, etc.)
    Kept separate from User so auth logic stays clean and independent.
    Has a One-to-One relationship with User.
    Enrolled courses are tracked via UserCourse (section + semester included).
*/

@Entity
@Table(name = "PROFILE_TABLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String major;

    // e.g. Freshman, Sophomore, Junior, Senior
    @Column
    private String year;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<UserCourse> enrollments;
}
