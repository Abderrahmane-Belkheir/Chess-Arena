package org.Core.User.Services;

import lombok.RequiredArgsConstructor;
import org.Core.User.Api.Dto.UserSummary;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepo userRepo;

    public UserSummary search(String userId){

        return null;
    }

    public UserSummary getMyProfile(String currentUserId){
        User user= userRepo.findById(currentUserId).orElseThrow();
        return new UserSummary(user.getId(),user.getUsername(),user.getElo(),"hello");
    }

}
