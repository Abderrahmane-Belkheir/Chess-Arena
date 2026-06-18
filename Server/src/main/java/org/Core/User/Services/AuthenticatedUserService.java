
package org.Core.User.Services;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

     public String getCurrentUser(){
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if(authentication==null||!(authentication.getPrincipal() instanceof Jwt)){
                throw new AuthenticationCredentialsNotFoundException("User not authenticated");
            }
            String userId=((Jwt) authentication.getPrincipal()).getSubject();
            if(userId==null){
                throw new AuthenticationCredentialsNotFoundException("something went wrong trying to authenticate you please try later");
            }
            return userId;
        }

    }

