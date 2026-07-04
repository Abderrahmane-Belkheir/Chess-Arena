package org.Core.GameLogic.Services.MoveValidation;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Api.Dto.MoveResponse;
import org.Core.GameLogic.Exceptions.IllegalMoveException;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Persistence.GameRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameMoveValidation {

    private final GameSessionRegistry gameSessionRegistry;
    private final GameRepo gameRepo;

    public MoveResponse validateAndPlay(MoveRequest request){
        String from=request.getFrom();
        String to=request.getTo();

        if (from.equalsIgnoreCase(to)) {
            throw new IllegalMoveException("From and to squares are identical");
        }

        Square fromSq=Square.fromValue(from.toUpperCase());
        Square toSq=Square.fromValue(to.toUpperCase());

        if (fromSq == Square.NONE || toSq == Square.NONE) {
            throw new IllegalMoveException("Invalid square: " + from + " or " + to);
        }

        Board board=gameSessionRegistry.getBoard(request.getGameId()).orElseGet(()->{
            Game game=gameRepo.findById(request.getGameId()).orElseThrow();
           Board b=new Board();
            b.loadFromFen(game.getFen());
            gameSessionRegistry.createSession(game.getId(),game.getFen());
            return b;
        });

        Move move=new Move(fromSq,toSq);
        if(!board.isMoveLegal(move,true)){
            throw new IllegalMoveException("Illegal move: " + from + " → " + to);
        }
        board.doMove(move);
        return new MoveResponse(from,to,board.getFen());
    }

}
