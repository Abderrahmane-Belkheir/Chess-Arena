package org.Core.Configurations.Websocket;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Social.Game.GameSpectator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;



@Component
@Slf4j
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;



    private final GameSpectator spectator;
    private static final String SPECTATE_PREFIX = "/topic/spectate/";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                log.warn("Missing Authorization header on CONNECT");
                throw new MessageDeliveryException("Missing Authorization header");
            }

            try {
                String token = header.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                 String userId = jwt.getSubject();
                accessor.setUser(jwt::getSubject);
                return message;
            } catch (JwtException e) {
                log.error("JWT decode failed: {}", e.getMessage());
                throw new MessageDeliveryException("Invalid token: " + e.getMessage());
            }
        }else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            String destination = accessor.getDestination();

            if (destination == null || !destination.startsWith(SPECTATE_PREFIX)) {
                return message;
            }

            String targetUserId = destination.substring(SPECTATE_PREFIX.length());

//            if (!spectator.isApproved(Integer.parseInt(targetUserId),accessor.getUser().getName())) {
//                return null;
//            }
        }

        return message;
    }


}
