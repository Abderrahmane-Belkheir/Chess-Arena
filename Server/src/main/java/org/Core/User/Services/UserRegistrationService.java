package org.Core.User.Services;

import lombok.RequiredArgsConstructor;
import org.Core.User.Api.Dto.UserRegistration;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepo userRepo;

    public void register(UserRegistration userRegistrationDTO) {


        try {

            User user = User.builder()
                    .id(userRegistrationDTO.getUserId())
                    .username(userRegistrationDTO.getName())
                    .email(userRegistrationDTO.getEmail())
                    .build();

            userRepo.save(user);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
