package org.Core.Auth;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.Core.Auth.Exceptions.TokenExpiredException;
import org.Core.Shared.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
public class AuthService {

    private final String issuerUrl;
    private final String clientId;
    private final String localUrl;

    private final AuthClient authClient;
    private final AppConfig appConfig;
    private final TokenStorage tokenStorage;

    private String codeVerifier;
    private String state;



    @Inject
    public AuthService(
            AuthClient authClient,
            AppConfig appConfig,
            TokenStorage tokenStorage
    ) {
        this.authClient = authClient;
        this.appConfig = appConfig;
        this.tokenStorage = tokenStorage;

        this.issuerUrl = appConfig.get("auth.issuer.url");
        this.clientId = appConfig.get("client.id");
        this.localUrl = appConfig.get("local.callback.url");
    }

    public boolean isUserAuthenticated()
            throws IOException, InterruptedException {

        Optional<String> refreshToken =
                tokenStorage.loadRefreshToken();

        if (refreshToken.isEmpty()) {
            return false;
        }

        try {
            AuthTokens tokens =
                    authClient.exchangeRefreshToken(
                            refreshToken.get()
                    );

            tokenStorage.setAccessToken(
                    tokens.getAccessToken()
            );
            tokenStorage.saveRefreshToken(
                    tokens.getRefreshToken()
            );

            // FETCHING USER PROFILE
            return true;

        } catch (TokenExpiredException e) {
            return false;
        }
    }

    // =========================
    // REDIRECT URL
    // =========================

    public String redirect() throws Exception {

        generatePkceAndState();

        return  issuerUrl
                + "/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&scope=openid%20profile%20email%20offline_access"
                + "&state=" + state
                + "&redirect_uri=" + URLEncoder.encode(localUrl, StandardCharsets.UTF_8)
                + "&code_challenge=" + getCodeChallenge()
                + "&code_challenge_method=S256"
                + "&prompt=consent";
    }

    // =========================
    // START CALLBACK SERVER
    // =========================

    public CompletableFuture<Boolean> callbackServer() {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Thread serverThread = new Thread(() -> {

            try {
                HttpServer server = HttpServer.create(
                        new InetSocketAddress(3000),
                        0
                );

                server.createContext("/callback", exchange -> {
                    try {

                        URI uri = exchange.getRequestURI();
                        Map<String, String> params = parseQuery(uri.getQuery());

                        String code = params.get("code");
                        String receivedState = params.get("state");

                        if (code == null || !state.equals(receivedState)) {

                            future.complete(false);
                            send(exchange, "Invalid login");
                            server.stop(0);
                            return;
                        }

                        exchangeCodeForToken(code);

                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

                        send(exchange, loadLoginSuccess("/login-success.html"));

                        future.complete(true);
                        server.stop(0);

                    } catch (Exception e) {
                        future.complete(false);
                        server.stop(0);
                    }
                });

                server.start();

            } catch (Exception e) {
                future.complete(false);
            }

        });

        serverThread.setDaemon(true);
        serverThread.start();

        return future;
    }


    public String loadLoginSuccess(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("File not found: " + path);

            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void send(HttpExchange exchange, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }



    // =========================
    // EXCHANGE CODE
    // =========================

    private void exchangeCodeForToken(String code)
            throws IOException, InterruptedException {
        AuthTokens tokens =
                authClient.exchangeCode(
                        code,
                        codeVerifier
                );

        tokenStorage.setAccessToken(
                tokens.getAccessToken()
        );

        tokenStorage.saveRefreshToken(
                tokens.getRefreshToken()
        );
    }

    // =========================
    // PKCE
    // =========================

    private void generatePkceAndState() {

        SecureRandom random = new SecureRandom();

        codeVerifier = generateRandomBase64Url(random);
        state = generateRandomBase64Url(random);
    }

    private String generateRandomBase64Url(SecureRandom random) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String getCodeChallenge()
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        codeVerifier.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }

    private static Map<String, String> parseQuery(
            String query
    ) {

        Map<String, String> result =
                new HashMap<>();

        if (query == null) {
            return result;
        }

        for (String param : query.split("&")) {

            String[] entry =
                    param.split("=");

            if (entry.length > 1) {

                result.put(
                        entry[0],
                        URLDecoder.decode(
                                entry[1],
                                StandardCharsets.UTF_8
                        )
                );
            }
        }

        return result;
    }

}