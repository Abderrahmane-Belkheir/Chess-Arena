package org.Core.Config;


import com.google.inject.Inject;
import org.Core.Auth.AuthClient;
import org.Core.Auth.AuthTokens;
import org.Core.Auth.TokenStorage;
import tools.jackson.databind.ObjectMapper;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TokenStorage tokenStorage;
    private final AuthClient authClient;
    private final AppConfig appConfig;
    private final String serverUrl;

    @Inject
    public ApiClient(ObjectMapper objectMapper, TokenStorage tokenStorage,AuthClient authClient,AppConfig appConfig) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper=objectMapper;
        this.tokenStorage=tokenStorage;
        this.authClient=authClient;
        this.appConfig=appConfig;
        this.serverUrl=appConfig.get("server.url");
    }

    public <T extends DTO> T GET(String url,Class<T> clazz) throws IOException, InterruptedException {
        return execute( buildRequest(url,"GET",null), clazz );
    }

    public <T extends DTO,V extends DTO> T POST(V body,String url,Class<T> clazz) throws IOException, InterruptedException {
        return execute( buildRequest(url,"POST",objectMapper.writeValueAsString(body)) , clazz );
    }

    public <T extends DTO,V extends DTO> T DELETE(V body,String url,Class<T> clazz)throws IOException, InterruptedException {
        return execute( buildRequest(url,"DELETE",objectMapper.writeValueAsString(body)) , clazz );
    }

    public <T extends DTO,V extends DTO> T PUT(V body,String url,Class<T> clazz) throws IOException, InterruptedException {
        return execute( buildRequest(url,"PUT",objectMapper.writeValueAsString(body)) , clazz );
    }

    private   <T extends DTO> T execute(HttpRequest req,Class<T> clazz) throws IOException, InterruptedException {
        HttpResponse<String> response =
                httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            boolean refreshed = tryRefresh();
            if (refreshed) {

                req = rebuildWithNewToken(req);
                response = httpClient.send(req,
                        HttpResponse.BodyHandlers.ofString());
            } else {
                throw new AuthenticationException();
            }
        }

        if(clazz==null) return null;

        return objectMapper.readValue(response.body(),clazz);
    }

    private HttpRequest rebuildWithNewToken(HttpRequest original) {
        return HttpRequest.newBuilder(original.uri())
                .header("Authorization", "Bearer " + tokenStorage.getAccessToken())
                .header("Content-Type", "application/json")
                .method(original.method(),
                        original.bodyPublisher()
                                .orElse(HttpRequest.BodyPublishers.noBody()))
                .build();
    }

    private boolean tryRefresh() {
        try {
            String refreshToken = tokenStorage.loadRefreshToken()
                    .orElseThrow();
                AuthTokens tokens = authClient.exchangeRefreshToken(refreshToken);
            tokenStorage.setAccessToken(tokens.getAccessToken());
            tokenStorage.saveRefreshToken(tokens.getRefreshToken());
            return true;
        } catch (Exception e) {
            tokenStorage.clearRefreshToken();
            return false;
        }
    }

    private HttpRequest buildRequest(String url, String method, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl+url))
                .header("Authorization", "Bearer " + tokenStorage.getAccessToken())
                .header("Content-Type", "application/json");

        builder = switch (method) {
            case "GET"    -> builder.GET();
            case "DELETE" -> builder.DELETE();
            case "POST"   -> builder.POST(body(body));
            case "PUT"    -> builder.PUT(body(body));
            default       -> throw new IllegalArgumentException("Unknown method: " + method);
        };

        return builder.build();
    }
    private HttpRequest.BodyPublisher body(String json) {
        return json != null
                ? HttpRequest.BodyPublishers.ofString(json)
                : HttpRequest.BodyPublishers.noBody();
    }
}
