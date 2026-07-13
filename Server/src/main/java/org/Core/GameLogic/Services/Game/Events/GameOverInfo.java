package org.Core.GameLogic.Services.Game.Events;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class GameOverInfo extends GameEvent {
    String userId;
    private GameResult result;
    private EndReason endReason;

    public GameOverInfo(GameResult result,EndReason endReason) {
        this.endReason = endReason;
        this.result=result;
    }

    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED,DRAW}

}
