package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Models.GameSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GameSessionStore {

    private static final String PREFIX  = "game:";
    private static final long   TTL_HRS = 2;

    private final RedisTemplate<String, String> redis;

    // ── Write ─────────────────────────────────────────────────────────

    public void save(String gameId, GameSession session) {
        String key = PREFIX + gameId;
        redis.opsForHash().putAll(key, toMap(session));
        redis.expire(key, Duration.ofHours(TTL_HRS));
    }

    // ── Read ──────────────────────────────────────────────────────────

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
        return Map.of(
                "gameId",       session.getGameId(),
                "type",session.getType().name(),
                "whiteId",      session.getWhitePlayerId(),
                "blackId",      session.getBlackPlayerId(),
                "turn",session.getTurn().name(),
                "status",       String.valueOf(session.isActive()),
                "whitePlayedTime",  String.valueOf(session.getWhitePlayedTime()),
                "blackPlayedTime",String.valueOf(session.getBlackPlayedTime()),
                "lastMoveAt",  String.valueOf(session.getLastMoveAt())
        );
    }

    private GameSession fromMap(Map<Object, Object> m) {
        return new GameSession(
                (String) m.get("gameId"),
                Game.GameType.valueOf((String) m.get("type")),
                (String) m.get("whiteId"),
                (String) m.get("blackId"),
                Color.valueOf((String)(m.get("turn"))),
                Boolean.parseBoolean(((String)m.get("status"))),
                Long.parseLong((String)m.get("whitePlayedTime")),
                Long.parseLong((String)m.get("blackPlayedTime")),
                Instant.parse((String) m.get("lastMoveAt"))
        );
    }
}