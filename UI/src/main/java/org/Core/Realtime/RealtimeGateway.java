package org.Core.Realtime;

import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.CompletableFuture;

public interface RealtimeGateway {
    void startLobbyPING();

   void startGameSearching();

     void stopGameSearching();

    CompletableFuture<Void> connect();

    void subscribe( String destination, Class<?> payloadType);
}
