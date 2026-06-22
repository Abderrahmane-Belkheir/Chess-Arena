package org.Core.Realtime;

import com.google.inject.Inject;
import org.Core.Auth.TokenStorage;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.converter.*;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class Websocket {

    private StompSession session;
    private final TokenStorage tokenStorage;
    private final ScheduledExecutorService executorService= Executors.newScheduledThreadPool(1);
    private  ScheduledFuture<?> lobbyPinging;
    private ScheduledFuture<?> playPinging;


    @Inject
    public Websocket(TokenStorage tokenStorage){this.tokenStorage=tokenStorage;}

    public CompletableFuture<Void>  connect() {

                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            List<MessageConverter> converters = new ArrayList<>();
            converters.add(new StringMessageConverter());
            converters.add(new ByteArrayMessageConverter());

            stompClient.setMessageConverter(new CompositeMessageConverter(converters));


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
                    .thenAccept(stompSession -> {
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

    public void startPlayPING(){
      session.send("/app/play","");
    }

    }


