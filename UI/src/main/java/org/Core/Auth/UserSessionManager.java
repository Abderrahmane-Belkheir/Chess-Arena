package org.Core.Auth;

import com.google.inject.Inject;
import org.Core.Auth.DTO.UserSession;
import org.Core.Shared.ApiClient;
import org.Core.Shared.AppConfig;

import java.io.IOException;


public class UserSessionManager {

    private final ApiClient apiClient;
    private final AppConfig appConfig;
    private final String serverUrl;

    @Inject
    public UserSessionManager(ApiClient apiClient, AppConfig appConfig, String serverUrl) {
        this.apiClient=apiClient;
        this.appConfig=appConfig;
        this.serverUrl=appConfig.get("server.url");
    }

    public UserSession getUserSession() throws IOException, InterruptedException {
        return  apiClient.GET(serverUrl+"/api/v1/users/me",UserSession.class);
    }

}
