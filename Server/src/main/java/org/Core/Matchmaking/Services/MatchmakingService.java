package org.Core.Matchmaking.Services;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import org.Core.Matchmaking.Api.Dto.GameFound;
import org.Core.Matchmaking.Api.Dto.PlayerMove;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper;



    public void searchGame(String userId) throws InterruptedException {
        String fen="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        Side side=Side.BLACK;
        GameFound gameFound = new GameFound(true, "123",
                new GameFound.Opponent(123, "player2", 1200, ""), fen,side);
        String json1 = mapper.writeValueAsString(gameFound);
        Thread.sleep(15000);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/matchmaking",
                json1);

        Board board=new Board();
        board.loadFromFen(fen);
        while(!board.isMated()){
        List<Move> legalMoves=board.legalMoves();
        Move move=legalMoves.get(new Random().nextInt(board.legalMoves().size()));
        board.doMove(move);
        Thread.sleep(3000);
        String json2=mapper.writeValueAsString(new PlayerMove(move.getFrom().value(),move.getTo().value(),board.getFen()));
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/game.move",
                json2);
        // THIS IS A DEMO TEST
    }
    }

}
