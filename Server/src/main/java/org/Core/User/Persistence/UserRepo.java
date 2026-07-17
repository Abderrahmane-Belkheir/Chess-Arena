package org.Core.User.Persistence;

import org.Core.User.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,String> {

    boolean existsByPublicId(int id);

    @Transactional
    @Modifying
    @Query("UPDATE User u  SET u.status=:status WHERE u.id=:userOneId OR u.id=:userTwoId")
    void updateUsersStatus(@Param("status")User.Status status,@Param("userOneId") String id1,@Param("userTwoId") String id2);

    Optional<User> findByPublicId(int userId);

    @Query(value = "SELECT u.id As UserId FROM users u WHERE u.public_id=:publicId",nativeQuery = true)
    internalId getInternalId(@Param("publicId") int publicId);

    interface internalId{
        String getUserId();
    }

}
