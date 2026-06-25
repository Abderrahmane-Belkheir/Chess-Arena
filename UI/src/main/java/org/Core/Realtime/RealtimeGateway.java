package org.Core.Realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.Core.Auth.TokenStorage;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.OpponentMove;
import org.Core.Game.Events.PlayerMove;
import org.Core.Config.GameEventPublisher;
import org.springframework.messaging.converter.StringMessageConverter;
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

    private final GameEventPublisher appEvents;
    private final TokenStorage tokenStorage;
    private final ObjectMapper objectMapper;

    private final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
    private  ScheduledFuture<?> lobbyPinging;


    @Inject
    public RealtimeGateway(TokenStorage tokenStorage, GameEventPublisher appEvents, ObjectMapper objectMapper){
        this.tokenStorage=tokenStorage;
        this.appEvents=appEvents;
        this.objectMapper=objectMapper;
    }

    public CompletableFuture<Void>  connect() {
        WebSocketStompClient stompClient=new WebSocketStompClient(new StandardWebSocketClient());
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
                                    System.out.println("CONNECTED: " + s.getSessionId());
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

//    @Subscribe
//    public void sendMove(PlayerMove playerMove){
//    session.send("/app/game.move","");
//    }

    private void subscribe(StompSession s){
            subscribeToSingle(s, "/user/queue/matchmaking", GameFound.class);
            subscribeToSingle(s, "/user/queue/game.move", OpponentMove.class);
    }

    private  void subscribeToSingle(StompSession s,String destination,Class<?> clazz){
        s.subscribe(
                destination,
                new StompFrameHandler() {

                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(
                            StompHeaders headers,
                            Object payload) {
                        if (!(payload instanceof String)) {
                            System.err.println("Unexpected payload type: " + payload.getClass());
                            return;
                        }
                        String json = (String) payload;
                        Object event = null;
                        try {
                            event = objectMapper.readValue(json, clazz);
                        }catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        appEvents.post(event);
                    }
                });
    }

}
