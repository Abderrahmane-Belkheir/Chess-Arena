package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip;
import org.Core.User.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendShipRepo extends JpaRepository<FriendShip,String> {

    @Query("SELECT Count(f) > 0 FROM FriendShip f WHERE (f.userOne.id=:userOneId AND f.userTwo.id=:userTwoId) OR (f.userOne.id=:userTwoId AND f.userTwo.id=:userOneId)")
    boolean doesFriendShipExists(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);

    @Query("SELECT f FROM FriendShip f WHERE (f.userOne.id=:userOneId AND f.userTwo.id=:userTwoId) OR (f.userOne.id=:userTwoId AND f.userTwo.id=:userOneId)")
    Optional<FriendShip> findFriendShip(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);

    // Hibernate can't put an entity reference (f.userTwo / f.userOne) inside a
    // CASE WHEN branch — it throws a ClassCastException trying to treat the
    // entity persister as a basic-valued mapping. So this is split into two
    // plain entity-projection queries and merged below instead of one CASE query.
    @Query("SELECT f.userTwo FROM FriendShip f WHERE f.userOne.id = :userId")
    List<User> findFriendsAsUserOne(@Param("userId") String userId);

    @Query("SELECT f.userOne FROM FriendShip f WHERE f.userTwo.id = :userId")
    List<User> findFriendsAsUserTwo(@Param("userId") String userId);

    default List<User> findFriendsOf(String userId) {
        List<User> friends = new ArrayList<>(findFriendsAsUserOne(userId));
        friends.addAll(findFriendsAsUserTwo(userId));
        return friends;
    }

}
