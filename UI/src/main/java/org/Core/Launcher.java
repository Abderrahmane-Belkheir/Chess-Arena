package org.Core;

import org.Core.UI.ChessArenaGame;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Launcher {
    public static void main(String[] args){
        ChessArenaGame.main(args);
    }
    public class WebSocketTest {

        public static void main() throws Exception {

            String token = "YOUR_JWT_TOKEN_HERE"; // paste a real token

            WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new JacksonJsonMessageConverter());

            WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
            httpHeaders.add("Authorization", "Bearer " + token);

            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.add("Authorization", "Bearer " + token);

            System.out.println("Connecting...");

            CompletableFuture<StompSession> future = stompClient.connectAsync(
                    "ws://localhost:8080/ws",
                    httpHeaders,
                    stompHeaders,
                    new StompSessionHandlerAdapter() {

                        @Override
                        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                            System.out.println(">>> afterConnected: " + session.getSessionId());
                        }

                        @Override
                        public void handleTransportError(StompSession session, Throwable exception) {
                            System.err.println(">>> Transport error: " + exception.getMessage());
                            exception.printStackTrace();
                        }

                        @Override
                        public void handleException(StompSession session, StompCommand command,
                                                    StompHeaders headers, byte[] payload, Throwable exception) {
                            System.err.println(">>> handleException: " + exception.getMessage());
                            exception.printStackTrace();
                        }
                    }
            );

            try {
                StompSession session = future.get(10, TimeUnit.SECONDS); // hard timeout
                System.out.println(">>> Future resolved, session: " + session.getSessionId());

                // Keep alive so you can observe behavior
                Thread.sleep(5000);

                session.disconnect();
                System.out.println(">>> Disconnected cleanly");

            } catch (TimeoutException e) {
                System.err.println(">>> TIMED OUT — server never completed handshake");
            } catch (ExecutionException e) {
                System.err.println(">>> EXECUTION FAILED: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }

            System.exit(0);
        }
    }
}
