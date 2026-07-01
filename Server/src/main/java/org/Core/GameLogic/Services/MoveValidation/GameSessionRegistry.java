package org.Core.GameLogic.Services.MoveValidation;

import com.github.bhlangonijr.chesslib.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
class GameSessionRegistry {

    private final Map<String, Board> activeSessions=new ConcurrentHashMap<>();

    public void createSession(String gameId, String fen) {
        Board board = new Board();
        board.loadFromFen(fen);
        activeSessions.put(gameId, board);
    }

    public Board getBoard(String gameId) {
        return activeSessions.get(gameId);
    }

    public void removeSession(String gameId) {
        activeSessions.remove(gameId);
    }

    public boolean isActive(String gameId) {
        return activeSessions.containsKey(gameId);
    }
}
