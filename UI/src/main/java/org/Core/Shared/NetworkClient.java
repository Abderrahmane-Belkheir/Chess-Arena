package org.Core.Shared;

import java.net.http.HttpClient;
import java.time.Duration;

public class NetworkClient {
    private final HttpClient httpClient;

    public NetworkClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpClient getClient() {
        return httpClient;
    }
}
