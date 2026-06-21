package org.Core.Auth;

import com.google.inject.Inject;
import org.Core.Auth.DTO.UserSession;
import org.Core.Shared.ApiClient;
import org.Core.Shared.AppConfig;
import org.Core.Social.DTO.UserSummary;

import java.io.IOException;


public class UserSessionManager {

    private final ApiClient apiClient;


    @Inject
    public UserSessionManager(ApiClient apiClient) {
        this.apiClient=apiClient;
    }

    public UserSession getUserSession() throws IOException, InterruptedException {
       return  apiClient.GET("/api/v1/users/me",UserSession.class);
    }

}
