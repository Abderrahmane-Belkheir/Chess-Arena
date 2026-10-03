package org.Core.Realtime;


import com.google.inject.Inject;
import lombok.Getter;
import org.Core.Auth.TokenStorage;
import org.Core.Config.AppConfig;
import org.Core.Game.Events.*;
import org.Core.Config.GameEventPublisher;
import org.Core.Social.Events.SocialEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.*;



public class GameRealtimeGatewayStub implements RealtimeGateway {

    private static final Logger log = LoggerFactory.getLogger(GameRealtimeGatewayStub.class);
    @Getter
    private static StompSession session;

    private final GameEventPublisher appEvents;
    private final TokenStorage tokenStorage;
    private final AppConfig appConfig;

    private final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
    private  ScheduledFuture<?> lobbyPinging;

    private StompSession.Subscription gameEventsSubscription;
    private StompSession.Subscription spectateResponseSubscription;
    private StompSession.Subscription spectateTopicSubscription;

    @Inject
    public GameRealtimeGatewayStub(TokenStorage tokenStorage, GameEventPublisher appEvents, AppConfig appConfig){
        this.tokenStorage=tokenStorage;
        this.appEvents=appEvents;
        this.appConfig=appConfig;
    }

    @Override
    public CompletableFuture<Void>  connect() {
        WebSocketStompClient stompClient=new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + tokenStorage.getAccessToken());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " +tokenStorage.getAccessToken());
        return stompClient
                .connectAsync(
                        "ws://localhost:8080/ws",
                        httpHeaders,
                        stompHeaders,
                        new StompSessionHandlerAdapter() {

                            @Override
                            public void afterConnected(StompSession s, StompHeaders connectedHeaders) {
                                System.out.println("CONNECTED");

                            }
                        })
                .orTimeout(10, TimeUnit.SECONDS)
                .thenAccept(s -> {
                    session = s;
                    subscribe("/user/queue/social", SocialEvent.class);
                    // GameFound now also arrives here for challenge-accepted
                    // games, not just matchmaking — that can happen at any
                    // time you're connected, not just while searching, so it
                    // needs the same session-long subscription social gets
                    // instead of being scoped to startGameSearching().
                    subscribe("/user/queue/matchmaking", GameFound.class);
                })
                .exceptionally(ex -> {
                    System.err.println(">>> Connection failed: " + ex.getMessage());
                    return null;
                });

    }


    @Override
    public void startPING(){
        lobbyPinging=executorService
                .scheduleAtFixedRate(()-> session.send("/app/online",""),0,30,TimeUnit.SECONDS);
    }

    @Override
    public void shutdown(){
        if (lobbyPinging != null) {
            lobbyPinging.cancel(true);
        }
        executorService.shutdownNow();
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }


    @Override
    public void startGameSearching(){
        session.send("/app/start.search","");
    }

    @Override
    public void stopGameSearching(){
        session.send("/app/stop.search","");
    }

    @Override
    public void stopSpectating(){
        unSubscribe(spectateTopicSubscription);
        spectateTopicSubscription = null;
    }

    @Override
    public void subscribe( String destination, Class<?> payloadType) {

        StompSession.Subscription subscription = session.subscribe(
                destination,
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return payloadType;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {

                        if (!payloadType.isInstance(payload)) {
                            System.err.println("Unexpected payload type: " + payload.getClass());
                            return;
                        }
                        if (payload instanceof GameFound) {
                            subscribe( "/user/queue/game.events", GameEvent.class);
                            subscribe("/user/queue/spectate.requests",SpectatedResponse.class);
                        } else if (payload instanceof GameOverInfo) {
                            unSubscribe(gameEventsSubscription);
                        }else if(payload instanceof SpectatorResponse response){
                            subscribe("/topic/spectate/"+response.getSpectatedPlayer().getId(),GameEvent.class);
                            // Got the game we're joining — the "waiting for accept" queue has nothing left to tell us.
                            unSubscribe(spectateResponseSubscription);
                            spectateResponseSubscription = null;
                        }
                        appEvents.post(payload);
                    }
                });

        if (payloadType == GameEvent.class && destination.startsWith("/topic/spectate/")) {
            // Distinct from gameEventsSubscription below — both use GameEvent as
            // the payload type, but this is a spectator's read-only feed for
            // someone else's game, not our own /user/queue/game.events.
            spectateTopicSubscription = subscription;
        } else if (payloadType == GameEvent.class) {
            gameEventsSubscription = subscription;
        } else if (payloadType == SpectatorResponse.class) {
            spectateResponseSubscription = subscription;
        }

    }
    private void unSubscribe(StompSession.Subscription subscription){
        if (subscription != null) {
           subscription.unsubscribe();
           subscription= null;
        }
    }
}
