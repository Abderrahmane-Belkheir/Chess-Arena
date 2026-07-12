package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.Color;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrawOfferStore {

    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Duration DRAW_OFFER_TTL = Duration.ofSeconds(60);

    private String key(String gameId) {
        return "draw-offer:" + gameId;
    }

    public boolean offerDraw(String gameId, Color offeredBy) {
        String key = key(gameId);
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(
                key,
                mapper.writeValueAsString(new DrawOffer(offeredBy,Instant.now())),
                DRAW_OFFER_TTL
        );
        return Boolean.TRUE.equals(wasSet);
    }

    public Optional<DrawOffer> get(String gameId) {
        String json = redisTemplate.opsForValue().get(key(gameId));
        return json == null ? Optional.empty() : Optional.of(mapper.readValue(json,DrawOffer.class));
    }

    public void clear(String gameId) {
        redisTemplate.delete(key(gameId));
    }
}