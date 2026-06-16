package org.Core.User.Api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.Core.User.Api.dto.UserRegistrationDTO;

@RestController
@RequestMapping("/api/v1/users")
public class controller {
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody UserRegistrationDTO u){
        System.out.println(u);
        return ResponseEntity.noContent().build();
    }
}
