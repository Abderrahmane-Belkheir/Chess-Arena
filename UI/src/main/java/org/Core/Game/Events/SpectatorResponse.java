package org.Core.Game.Events;

import com.github.bhlangonijr.chesslib.Side;
import lombok.Data;

@Data
public class SpectatorResponse {
     private int spectatedId;
     private Side side;
     private String fen;
}
