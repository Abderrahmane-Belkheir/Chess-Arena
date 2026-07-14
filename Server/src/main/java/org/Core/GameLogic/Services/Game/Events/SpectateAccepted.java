package org.Core.GameLogic.Services.Game.Events;

import com.github.bhlangonijr.chesslib.Side;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class SpectateAccepted {
    private Side side;
    private String fen;
}
