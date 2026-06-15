package org.Core.Auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.Core.Shared.AppConfig;
import org.Core.Shared.NetworkClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.RemoteException;


@Slf4j
public class AuthClient {

    private final NetworkClient networkClient;
    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;
    private final String issuerUrl;
    private final String clientId;
    private final String localUrl;

    @Inject
    public AuthClient( NetworkClient networkClient,AppConfig appConfig,ObjectMapper objectMapper) {
        this.networkClient=networkClient;
        this.appConfig=appConfig;
        this.objectMapper=objectMapper;
        this.issuerUrl = appConfig.get("auth.issuer.url");
        this.clientId = appConfig.get("client.id");
        this.localUrl = appConfig.get("local.callback.url");
    }

    public AuthTokens exchangeRefreshToken(String token) throws IOException, InterruptedException {
        TokenRequest request = TokenRequest.builder()
                .grantType("refresh_token")
                .refreshToken(token)
                .clientId(clientId)
                .build();
        return executeExchange(request);
    }

    public AuthTokens exchangeCode(String code,String codeVerifier) throws IOException, InterruptedException {
        TokenRequest request = TokenRequest.builder()
                .grantType("authorization_code")
                .clientId(clientId)
                .code(code)
                .redirectUri(localUrl)
                .codeVerifier(codeVerifier)
                .build();
       return executeExchange(request);
    }

    private AuthTokens executeExchange(TokenRequest tokenRequest) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(issuerUrl + "/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequest.toUrlEncoded()))
                .build();

        HttpResponse<String> response =
                networkClient.getClient().send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode()<500&&400<=response.statusCode()){
            if(tokenRequest.getRefreshToken()!=null) System.out.println("REFRESH TOKEN EXPIRED ");
            throw new TokenExpiredException("");
        }

        if (response.statusCode() != 200) {
            System.out.println("Token exchange failed with status code "+response.statusCode());
            throw new IOException("Token exchange failed with status: " + response.statusCode() + " Body: " + response.body());
        }

        try {
            TokenResponse tokenResponse=objectMapper.readValue(response.body(),TokenResponse.class);
            return new AuthTokens(tokenResponse.getAccessToken(),tokenResponse.getRefreshToken());
        }catch (Exception e){
           System.out.println("parsing failed "+e.getMessage());
           throw new RuntimeException(e);
        }
    }

}
