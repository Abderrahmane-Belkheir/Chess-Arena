package org.Core.Game.Logic.Services.Ranking;

import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;

/** Each player's own previous/change/new elo from this game — ready to drop straight onto their GameOverInfo. */
public record EloSummaries(GameOverInfo.EloSummary playerA, GameOverInfo.EloSummary playerB){}
