package org.Core.Game.Events;


public record OpponentMove (
     String from,
     String to,
     String newFen,
     GameOverInfo gameOverInfo){}
