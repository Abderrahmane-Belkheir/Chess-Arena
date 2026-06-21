package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip_Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendShip_RequestRepo extends JpaRepository<FriendShip_Request,String> {

    Optional<FriendShip_Request> findBySenderIdAndRecipientId(String senderId, String receiverId);

    boolean existsBySenderIdAndRecipientId(String currentUserId, String userId);


}
