package org.Core.GameLogic.Services.MoveValidation;

import com.github.bhlangonijr.chesslib.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameSessionRegistry {

    private final Map<String, Board> activeSessions=new ConcurrentHashMap<>();

    public void createSession(String gameId, String fen) {
        Board board = new Board();
        board.loadFromFen(fen);
        activeSessions.put(gameId, board);
    }

    protected Optional<Board> getBoard(String gameId) {
        return Optional.ofNullable(activeSessions.get(gameId));
    }

    protected void removeSession(String gameId) {
        activeSessions.remove(gameId);
    }

    protected boolean isActive(String gameId) {
        return activeSessions.containsKey(gameId);
    }
}
