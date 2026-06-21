package org.Core.User.Persistence;

import org.Core.User.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,String> {

    boolean existsByPublicId(int id);

    Optional<User> findByPublicId(int userId);

    @Query(value = "SELECT u.id As UserId FROM users u WHERE u.public_id=:publicId",nativeQuery = true)
    internalId getInternalId(@Param("publicId") int publicId);

    interface internalId{
        String getUserId();
    }

}
