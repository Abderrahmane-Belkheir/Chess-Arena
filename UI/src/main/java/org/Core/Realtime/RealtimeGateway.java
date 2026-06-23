package org.Core.Realtime;

import com.google.inject.Inject;
import org.Core.Auth.TokenStorage;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.GameMove;
import org.Core.Shared.AppEvents;
import org.springframework.messaging.converter.*;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.*;


public class RealtimeGateway {

    private StompSession session;

    private final AppEvents appEvents;
    private final TokenStorage tokenStorage;


    private final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
    private  ScheduledFuture<?> lobbyPinging;
    private ScheduledFuture<?> playPinging;


    @Inject
    public RealtimeGateway(TokenStorage tokenStorage, AppEvents appEvents){
        this.tokenStorage=tokenStorage;
        this.appEvents=appEvents;
    }

    public CompletableFuture<Void>  connect() {

                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

            stompClient.setMessageConverter(new StringMessageConverter());


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
                                    session = s;
                                }
                            })
                .orTimeout(10, TimeUnit.SECONDS)
                    .thenAccept(this::subscribe)
                .exceptionally(ex -> {
                    System.err.println(">>> Connection failed: " + ex.getMessage());
                    return null;
                });

    }

    public void startLobbyPING(){
           lobbyPinging=executorService
                   .scheduleAtFixedRate(()-> session.send("/app/online",""),0,30,TimeUnit.SECONDS);
       }

    public void startGameSearching(){
      session.send("/app/start.search","");
    }

    public void stopGameSearching(){
    session.send("/app/stop.search","");
    }

    private void subscribe(StompSession session){
        try {
            subscribeToSingle(session,"/user/queue/matchmaking", GameFound.class);
            subscribeToSingle(session,"/user/queue/game.move",GameMove.class);
        }catch (Throwable e){
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private  void subscribeToSingle(StompSession session,String destination,Class<?> clazz){
        session.subscribe(
                destination,
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return clazz;
                    }

                    @Override
                    public void handleFrame(
                            StompHeaders headers,
                            Object payload) {
                        System.out.println(payload);
                        appEvents.post((payload));
                    }
                });
    }

}
