package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Models.GameSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameSessionStore {

    private static final String PREFIX  = "game:";
    private static final long   TTL_HRS = 2;

    private final Map<String,String> map=new ConcurrentHashMap<>();

    private final RedisTemplate<String, String> redis;

    // ── Write ─────────────────────────────────────────────────────────

    public void save(String gameId, GameSession session) {
        map.put(session.getBlackPlayerId(),gameId);
        map.put(session.getWhitePlayerId(),gameId);
        String key = PREFIX + gameId;
        redis.opsForHash().putAll(key, toMap(session));
        redis.expire(key, Duration.ofHours(TTL_HRS));
    }

    // ── Read ──────────────────────────────────────────────────────────

    public Optional<String> findGame(String userId){
        return Optional.of(map.get(userId));
    }

    public Optional<GameSession> find(String gameId) {
        Map<Object, Object> entries = redis.opsForHash().entries(PREFIX + gameId);
        if (entries.isEmpty()) return Optional.empty();
        return Optional.of(fromMap(entries));
    }

    public void remove(String gameId){
        redis.delete(PREFIX+gameId);
    }

    public void updateTurnAndPlayedTimeAndLastMoveAt(String gameId, Color color,Instant lastMoveAt,long playedTime){
        String key=color==Color.WHITE?"blackPlayedTime":"whitePlayedTime";
        redis.opsForHash().putAll(PREFIX + gameId, Map.of(
                "turn",color.name(),
                key,String.valueOf(playedTime)
                ,"lastMoveAt",String.valueOf(lastMoveAt)
        ));
    }

    // ── Mapping ───────────────────────────────────────────────────────

    private Map<String, String> toMap(GameSession session) {
        Map<String, String> map = new HashMap<>();

        map.put("gameId", session.getGameId());
        map.put("type", session.getType().name());
        map.put("whiteId", session.getWhitePlayerId());
        map.put("whitePublicId", String.valueOf(session.getWhitePlayerPublicId()));
        map.put("blackId", session.getBlackPlayerId());
        map.put("blackPublicId", String.valueOf(session.getBlackPlayerPublicId()));
        map.put("turn", session.getTurn().name());
        map.put("status", String.valueOf(session.isActive()));
        map.put("whitePlayedTime", String.valueOf(session.getWhitePlayedTime()));
        map.put("blackPlayedTime", String.valueOf(session.getBlackPlayedTime()));
        map.put("lastMoveAt", String.valueOf(session.getLastMoveAt()));

        return map;
    }

    private GameSession fromMap(Map<Object, Object> m) {
        return new GameSession(
                (String) m.get("gameId"),
                Game.GameType.valueOf((String) m.get("type")),
                (String) m.get("whiteId"),
                Integer.parseInt((String) m.get("whitePublicId")),
                (String) m.get("blackId"),
                Integer.parseInt((String) m.get("blackPublicId")),
                Color.valueOf((String)(m.get("turn"))),
                Boolean.parseBoolean(((String)m.get("status"))),
                Long.parseLong((String)m.get("whitePlayedTime")),
                Long.parseLong((String)m.get("blackPlayedTime")),
                Instant.parse((String) m.get("lastMoveAt"))
        );
    }
}