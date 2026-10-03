package org.Core.Game.Logic.Services.Game.Events;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public final class GameOverInfo extends GameEvent {
    private GameResult result;
    private EndReason endReason;
    // null for friendly games — rating doesn't change for those.
    private EloSummary eloSummary;

    public GameOverInfo(GameResult result) {
        this.result=result;
    }
    public GameOverInfo(GameResult result,EndReason endReason){
        this(result);
        this.endReason=endReason;
    }
    public GameOverInfo(GameResult result,EndReason endReason,EloSummary eloSummary){
        this(result,endReason);
        this.eloSummary=eloSummary;
    }
    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED,DRAW}

    /** This recipient's own elo — what it was before the game, what this game changed it by (positive or negative), and what it is now. */
    public record EloSummary(int previousElo, int change, int newElo){}

}
