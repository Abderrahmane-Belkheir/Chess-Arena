package org.Core.Game.Events;

import lombok.Data;

@Data
public class OpponentMove {
    private boolean gameOver;
    private GameResult result;
    private String from;
    private String to;
    private String newFen;
    public enum GameResult{CHECKMATE,STALEMATE,DRAW}
}
