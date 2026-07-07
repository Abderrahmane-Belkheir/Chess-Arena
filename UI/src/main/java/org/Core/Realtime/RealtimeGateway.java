package org.Core.Realtime;

import java.util.concurrent.CompletableFuture;

public interface RealtimeGateway {
    void startLobbyPING();

   void startGameSearching();

     void stopGameSearching();

    CompletableFuture<Void> connect();
}
