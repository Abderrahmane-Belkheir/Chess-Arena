package org.Core.GameLogic.Services.Game.Events;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class GameOverInfo extends GameEvent {
    private GameResult result;
    private EndReason endReason;

    public GameOverInfo(GameResult result) {
        this.result=result;
    }
    public GameOverInfo(GameResult result,EndReason endReason){
        this(result);
        this.endReason=endReason;
    }

    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED,DRAW}

}
