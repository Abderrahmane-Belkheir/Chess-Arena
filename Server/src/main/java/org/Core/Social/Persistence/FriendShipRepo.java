package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendShipRepo extends JpaRepository<FriendShip,String> {

    @Query("SELECT Count(f) > 0 FROM FriendShip f WHERE (f.userOne.id=:userOneId AND f.userTwo.id=:userTwoId) OR (f.userOne.id=:userTwoId OR f.userTwo.id=:userOneId)")
    boolean doesFriendShipExists(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);

    @Query("SELECT f FROM FriendShip f WHERE (f.userOne.id=:userOneId AND f.userTwo.id=:userTwoId) OR (f.userOne.id=:userTwoId OR f.userTwo.id=:userOneId)")
    Optional<FriendShip> findFriendShip(@Param("userOneId") String userOneId, @Param("userTwoId") String userTwoId);


}
