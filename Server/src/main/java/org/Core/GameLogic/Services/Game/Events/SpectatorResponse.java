package org.Core.GameLogic.Services.Game.Events;


import com.github.bhlangonijr.chesslib.Side;
import org.Core.GameLogic.Api.Dto.GameFound;



public record SpectatorResponse (
     String targetId,
     GameFound.Player spectatedPlayer,
     GameFound.Player opponent,
     long spectatedTimeMs,
     long otherTimeMs,
     Side spectatedSide,
     Side turn,
     String fen
){}
