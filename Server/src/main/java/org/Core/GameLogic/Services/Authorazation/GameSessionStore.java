package org.Core.GameLogic.Services.Authorazation;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.GameSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GameSessionStore {
    private static final String PREFIX  = "game:";
    private static final long   TTL_HRS = 2;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String gameId,GameSession session) {

            String json = objectMapper.writeValueAsString(session);

            redisTemplate.opsForValue()
                    .set(PREFIX + gameId, json, TTL_HRS, TimeUnit.HOURS);
    }

    public Optional<GameSession> find(String gameId) {

        String json = redisTemplate.opsForValue().get(PREFIX + gameId);

        if (json == null){return Optional.empty();}

        return Optional.of(objectMapper.readValue(json, GameSession.class));
    }

}
