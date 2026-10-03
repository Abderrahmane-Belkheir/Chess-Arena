package org.Core.Game.History;

import com.google.inject.Inject;
import org.Core.Config.ApiClient;
import org.Core.Game.History.DTO.GameHistory;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameHistoryClient {

    private final ApiClient apiClient;
    private final String baseUrl = "/api/v1/game";

    @Inject
    public GameHistoryClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public GameHistory fetchHistory() throws IOException, InterruptedException {
        return apiClient.GET(baseUrl + "/history", GameHistory.class);
    }

    public String fetchSummary(String gameId) throws IOException, InterruptedException {
        return apiClient.GET(baseUrl + "/summary/" + gameId);
    }
}
