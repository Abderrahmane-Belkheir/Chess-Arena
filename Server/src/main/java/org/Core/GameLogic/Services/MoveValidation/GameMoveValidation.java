package org.Core.GameLogic.Services.MoveValidation;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Api.Dto.*;
import org.Core.GameLogic.Exceptions.IllegalMoveException;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.GameLogic.Services.Game.GameOverHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameMoveValidation {

    private final GameSessionRegistry gameSessionRegistry;
    private final GameRepo gameRepo;

    public MoveOutCome processMove(MoveRequest request){
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
        GameOverEvent result=checkGameOver(board);
        boolean gameOver=result.playerA()!=null;
        GameOverInfo playerGameOverInfo=null;
        GameOverInfo opponentGameOverInfo=null;
        if(gameOver){
            opponentGameOverInfo=result.playerA().getResult()== GameOverInfo.GameResult.LOSS?result.playerA():result.playerB();
            playerGameOverInfo=result.playerA().getResult()== GameOverInfo.GameResult.WIN?result.playerA():result.playerB();
        }
        MoveResponse opponentPayload=new MoveResponse(from,to,board.getFen(),opponentGameOverInfo);
        return new MoveOutCome(gameOver,board.getFen(),opponentPayload,playerGameOverInfo);
    }

    private GameOverEvent checkGameOver(Board board){
        boolean gameOver=board.isMated()||board.isStaleMate()||board.isDraw();
        GameOverInfo loserInfo =null;
        GameOverInfo winnerInfo=null;
        if(gameOver){
            GameOverInfo.GameResult loserResult =board.isDraw()? GameOverInfo.GameResult.DRAW:GameOverInfo.GameResult.LOSS;
            GameOverInfo.GameResult winnerResult=board.isDraw()? GameOverInfo.GameResult.DRAW:GameOverInfo.GameResult.WIN;
            GameOverInfo.EndReason endReason=null;
            if (board.isMated()) {
                endReason=GameOverInfo.EndReason.CHECKMATE;
            } else if (board.isStaleMate()) {
                endReason =GameOverInfo.EndReason.STALEMATE;
            } else if (board.isDraw()) {
                endReason=GameOverInfo.EndReason.DRAW;
            }
            loserInfo =new GameOverInfo(loserResult,endReason);
            winnerInfo=new GameOverInfo(winnerResult,endReason);
        }

        return new GameOverEvent(winnerInfo,loserInfo);
    }

}

