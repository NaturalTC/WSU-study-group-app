package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Friendship;
import com.github.wsustudygroupapp.model.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** All accepted friendships where this profile is either side. */
    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = 'ACCEPTED'
        AND (f.sender.id = :profileId OR f.receiver.id = :profileId)
    """)
    List<Friendship> findAcceptedFriends(@Param("profileId") Long profileId);

    /** Pending requests sent TO this profile (they haven't responded yet). */
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    /** Pending requests sent BY this profile (waiting for the other person). */
    List<Friendship> findBySenderIdAndStatus(Long senderId, FriendshipStatus status);

    /** All friendships involving this profile in any direction and any status. */
    @Query("""
        SELECT f FROM Friendship f
        WHERE f.sender.id = :profileId OR f.receiver.id = :profileId
    """)
    List<Friendship> findAllInvolving(@Param("profileId") Long profileId);

    /** Find any existing relationship between two profiles regardless of direction. */
    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.sender.id = :a AND f.receiver.id = :b)
           OR (f.sender.id = :b AND f.receiver.id = :a)
    """)
    Optional<Friendship> findBetween(@Param("a") Long profileIdA, @Param("b") Long profileIdB);
}
