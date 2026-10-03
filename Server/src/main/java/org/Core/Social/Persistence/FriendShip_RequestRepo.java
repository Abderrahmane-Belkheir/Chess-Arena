package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip_Request;
import org.Core.User.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendShip_RequestRepo extends JpaRepository<FriendShip_Request,String> {

    Optional<FriendShip_Request> findBySenderIdAndRecipientId(String senderId, String receiverId);

    boolean existsBySenderIdAndRecipientId(String currentUserId, String userId);


    @Query("SELECT r FROM FriendShip_Request r " +
            "JOIN FETCH r.sender JOIN FETCH r.recipient " +
            "WHERE r.sender.id = :userId OR r.recipient.id = :userId")
    List<FriendShip_Request> findInvitationsOf(@Param("userId") String userId);

}
