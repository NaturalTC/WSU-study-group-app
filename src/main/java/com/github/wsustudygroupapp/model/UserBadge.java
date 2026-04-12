package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Junction table recording which badges a student has earned and when.
 * A student can only earn each badge once — enforced by the unique constraint below.
 */
@Entity
@Table(
    name = "user_badge_table",
    uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "badge_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student who earned this badge. */
    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    /** The badge that was earned. */
    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    /** When the badge was awarded. Set automatically on insert. */
    @Column(nullable = false)
    private LocalDateTime awardedAt;

    // TODO: Maicheal Shenouda — add a @PrePersist hook to auto-set awardedAt = LocalDateTime.now()
}
