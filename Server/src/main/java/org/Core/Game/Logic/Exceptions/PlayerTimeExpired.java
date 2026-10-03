package org.Core.Game.Logic.Exceptions;


import org.Core.Game.Logic.Models.Color;

public class PlayerTimeExpired extends RuntimeException {
    public PlayerTimeExpired(String gameId, Color player) {
        super("Player " + player + " has run out of time in game " + gameId);
    }
}
