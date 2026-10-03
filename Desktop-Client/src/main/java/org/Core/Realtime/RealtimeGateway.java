package org.Core.Realtime;

import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.CompletableFuture;

public interface RealtimeGateway {
    void startPING();

   void startGameSearching();

     void stopGameSearching();

     /** Unsubscribes from the spectated game's /topic/spectate/{id} feed. */
     void stopSpectating();

    CompletableFuture<Void> connect();

    void subscribe( String destination, Class<?> payloadType);

    /**
     * Stops the ping scheduler and disconnects the STOMP session. Call this
     * before marking the user offline on app shutdown — otherwise the ping
     * scheduler (a non-daemon thread pool, unaware the app is closing) can
     * fire one more /app/online after presence was just cleared, flipping
     * the user's friends back to seeing them as online.
     */
    void shutdown();
}
