package org.Core.GameLogic.Services.Authorization;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    public void updateTurn(String gameId,Color color){
        redis.opsForHash().put(PREFIX + gameId, "turn", color.name());
    }

    public void updateStatus(String gameId, String status) {
        redis.opsForHash().put(PREFIX + gameId, "status", status);
    }


    // ── Mapping ───────────────────────────────────────────────────────

    private Map<String, String> toMap(GameSession session) {
        return Map.of(
                "gameId",       session.getGameId(),
                "whiteId",      session.getWhitePlayerId(),
                "blackId",      session.getBlackPlayerId(),
                "turn",session.getTurn().name(),
                "status",       String.valueOf(session.isActive()),
                "lastWhiteMoveAt",  String.valueOf(session.getLastWhiteMoveAt()),
                "lastBlackMoveAt",  String.valueOf(session.getLastBlackMoveAt())
        );
    }

    private GameSession fromMap(Map<Object, Object> m) {
        return new GameSession(
                (String) m.get("gameId"),
                (String) m.get("whiteId"),
                (String) m.get("blackId"),
                Color.valueOf((String)(m.get("turn"))),
                Boolean.parseBoolean(((String)m.get("status"))),
                Long.parseLong((String)m.get("lastWhiteMoveAt")),
                Long.parseLong((String)m.get("lastBlackMoveAt"))
        );
    }
}