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
import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Services.Game.Events.MoveResponse;
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
        GameOverResult result=checkGameOver(board);
        GameOverInfo playerGameOverInfo=null;
        GameOverInfo opponentGameOverInfo=null;
        if(result.gameOver()){
            if(result.result()== GameOverInfo.GameResult.DRAW){
                playerGameOverInfo=new GameOverInfo(GameOverInfo.GameResult.DRAW);
                opponentGameOverInfo=new GameOverInfo(GameOverInfo.GameResult.DRAW);
            }else {
                playerGameOverInfo=new GameOverInfo(GameOverInfo.GameResult.WIN);
                opponentGameOverInfo=new GameOverInfo(GameOverInfo.GameResult.LOSS);
            }
            playerGameOverInfo.setEndReason(result.endReason());
            opponentGameOverInfo.setEndReason(result.endReason());
        }
        MoveResponse opponentPayload=new MoveResponse(from,to,board.getFen(),opponentGameOverInfo);
        return new MoveOutCome(result.gameOver(),board.getFen(),opponentPayload,playerGameOverInfo);
    }

    private GameOverResult checkGameOver(Board board){
        boolean gameOver=board.isMated()||board.isStaleMate()||board.isDraw();
        GameOverInfo.EndReason endReason=null;
        GameOverInfo.GameResult result=null;
        if(gameOver){
            if (board.isMated()) {
                result= GameOverInfo.GameResult.WIN;
                endReason=GameOverInfo.EndReason.CHECKMATE;
            } else if (board.isStaleMate()) {
                result= GameOverInfo.GameResult.DRAW;
                endReason =GameOverInfo.EndReason.STALEMATE;
            } else if (board.isDraw()) {
                result= GameOverInfo.GameResult.DRAW;
                endReason=GameOverInfo.EndReason.DRAW;
            }
        }
        return new GameOverResult(gameOver,result,endReason);
    }

}

