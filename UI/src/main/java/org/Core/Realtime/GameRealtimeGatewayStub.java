package org.Core.Realtime;


import com.google.inject.Inject;
import lombok.Getter;
import org.Core.Auth.TokenStorage;
import org.Core.Config.AppConfig;
import org.Core.Game.Events.*;
import org.Core.Config.GameEventPublisher;
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

    private StompSession.Subscription matchmakingSubscription;
    private StompSession.Subscription gameEventsSubscription;

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
                .thenAccept(s -> session = s)
                .exceptionally(ex -> {
                    System.err.println(">>> Connection failed: " + ex.getMessage());
                    return null;
                });

    }


    @Override
    public void startLobbyPING(){
        lobbyPinging=executorService
                .scheduleAtFixedRate(()-> session.send("/app/online",""),0,30,TimeUnit.SECONDS);
    }


    @Override
    public void startGameSearching(){
        session.send("/app/start.search","");
        subscribe( "/user/queue/matchmaking", GameFound.class);
    }


    @Override
    public void stopGameSearching(){
        session.send("/app/stop.search","");
        if (matchmakingSubscription != null) {
            matchmakingSubscription.unsubscribe();
            matchmakingSubscription = null;
        }
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
                            unSubscribe(matchmakingSubscription);
                        } else if (payload instanceof GameOverInfo) {
                            unSubscribe(gameEventsSubscription);
                        }else if(payload instanceof SpectatorResponse response){
                            subscribe("/topic/spectate/"+response.getSpectatedId(),null);
                        }
                        appEvents.post(payload);
                    }
                });

        if (payloadType == GameFound.class) {
            matchmakingSubscription = subscription;
        } else if (payloadType == GameEvent.class) {
            gameEventsSubscription = subscription;
        }

    }
    private void unSubscribe(StompSession.Subscription subscription){
        if (subscription != null) {
           subscription.unsubscribe();
           subscription= null;
        }
    }
}
