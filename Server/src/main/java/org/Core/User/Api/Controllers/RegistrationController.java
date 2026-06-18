package org.Core.User.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.User.Persistence.UserRepo;
import org.Core.User.Services.UserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.Core.User.Api.Dto.UserRegistration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class RegistrationController {

    private final UserRegistrationService registrationService;
    private final UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegistration u){
        registrationService.register(u);
        return ResponseEntity.noContent().build();
    }


}
