package org.Core.Game.Events;

import com.github.bhlangonijr.chesslib.Side;
import lombok.Data;

@Data
public  class SpectatorResponse  {
    private GameFound.Player spectatedPlayer;
    private GameFound.Player opponent;
    private long spectatedTimeMs;
    private long otherTimeMs;
    private Side spectatedSide;
    private Side turn;
    private String fen;
}
