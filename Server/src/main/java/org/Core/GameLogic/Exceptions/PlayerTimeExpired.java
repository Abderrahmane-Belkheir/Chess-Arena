package org.Core.GameLogic.Exceptions;

import org.Core.GameLogic.Models.Color;

public class PlayerTimeExpired extends RuntimeException {
    public PlayerTimeExpired(String gameId, Color player) {
        super("Player " + player + " has run out of time in game " + gameId);
    }
}
