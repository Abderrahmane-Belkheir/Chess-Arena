package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip_Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendShip_RequestRepo extends JpaRepository<FriendShip_Request,String> {
    boolean existBySenderIdAndReceiverId(String senderId,String receiverId);

    Optional<FriendShip_Request> findByBySenderIdAndReceiverId(String senderId,String receiverId);
}
