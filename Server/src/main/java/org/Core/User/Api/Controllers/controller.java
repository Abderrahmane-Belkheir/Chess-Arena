package org.Core.User.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.User.Api.Dto.UserSummary;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.Core.User.Services.UserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.Core.User.Api.Dto.UserRegistration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class controller {

    private final UserRegistrationService registrationService;
    private final UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegistration u){
        registrationService.register(u);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummary> me(@AuthenticationPrincipal Jwt jwt){
       System.out.println(jwt.getSubject());

       User user= userRepo.findById(jwt.getSubject()).orElseThrow();

    return ResponseEntity.ok(new UserSummary(user.getId(),user.getUsername(),user.getElo(),"hello"));
    }

}
