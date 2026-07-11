package org.Core.Realtime;


import com.google.inject.Inject;
import lombok.Getter;
import org.Core.Auth.TokenStorage;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.GameOverInfo;
import org.Core.Game.Events.MoveConfirmation;
import org.Core.Game.Events.OpponentMove;
import org.Core.Config.GameEventPublisher;
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


public class RealtimeGatewayStub implements RealtimeGateway {

    @Getter
    private static StompSession session;

    private final GameEventPublisher appEvents;
    private final TokenStorage tokenStorage;

    private final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
    private  ScheduledFuture<?> lobbyPinging;


    @Inject
    public RealtimeGatewayStub(TokenStorage tokenStorage, GameEventPublisher appEvents){
        this.tokenStorage=tokenStorage;
        this.appEvents=appEvents;
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
                    this.session = s;
                    subscribe(s);
                })
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
    }


    @Override
    public void stopGameSearching(){
        session.send("/app/stop.search","");
    }


    private void subscribe(StompSession s){
        subscribeToSingle(s, "/user/queue/matchmaking", GameFound.class);
        subscribeToSingle(s, "/user/queue/game.move", OpponentMove.class);
        subscribeToSingle(s,"/user/queue/game.over", GameOverInfo.class);
        subscribeToSingle(s,"/user/queue/game.move.confirm", MoveConfirmation.class);
    }

    private  void subscribeToSingle(StompSession s,String destination,Class<?> clazz){
        s.subscribe(
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
                        if (!(payload.getClass()==clazz)) {
                            System.err.println("Unexpected payload type: " + payload.getClass());
                            return;
                        }
                        appEvents.post(payload);
                    }
                });
    }

}
