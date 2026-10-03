package org.Core.Auth;

import com.google.inject.Inject;
import org.Core.Auth.DTO.UserSession;
import org.Core.Config.ApiClient;

import java.io.IOException;


public class UserSessionManager {

    private final ApiClient apiClient;
    private UserSession userSession;

    @Inject
    public UserSessionManager(ApiClient apiClient) {
        this.apiClient=apiClient;
    }

    public UserSession getUserSession(boolean fetchNew) throws IOException, InterruptedException {
        if(!fetchNew&&userSession!=null) return userSession;
        this.userSession=apiClient.GET("/api/v1/users/me",UserSession.class);
       return  userSession;
    }

}
