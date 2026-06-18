package org.Core.Social.Persistence;

import org.Core.Social.Models.FriendShip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendShipRepo extends JpaRepository<FriendShip,String> {
}
