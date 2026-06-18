package org.Core.User.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.User.Api.Dto.UserSummary;
import org.Core.User.Models.User;
import org.Core.User.Services.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping("/me")
    public ResponseEntity<UserSummary> me(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(userQueryService.getMyProfile(jwt.getSubject()));
    }

    @GetMapping("/search")
    public ResponseEntity<UserSummary> search(@RequestParam String publicId){
        return ResponseEntity.ok(userQueryService.search(publicId));
    }

}
