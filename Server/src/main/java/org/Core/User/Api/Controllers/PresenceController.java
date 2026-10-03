package org.Core.User.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.User.Services.AuthenticatedUserService;
import org.Core.User.Services.PresenceStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/presence")
public class PresenceController {

    private final PresenceStore presenceStore;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/offline")
    public ResponseEntity<Void> markOffline() {
        presenceStore.markOffline(authenticatedUserService.getCurrentUser());
        return ResponseEntity.noContent().build();
    }

}
