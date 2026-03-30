package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    This is an entity (Pojo - Plain Old Java Object) that is also a table in our MySQL database
    The purpose of this entity is for user verification / login usage + User Registration.
    All IDs are unique (Primary Key)
*/


@Entity
@Table(name = "USER_TABLE")
@Data  // generates all getters/setters
@NoArgsConstructor // generates a default constructor
@AllArgsConstructor // generates a constructor that takes in all parameters
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String role; // the purpose of this is for later admin access

    @Column
    private boolean isVerified = false;

    @Column
    private String verificationToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
}
