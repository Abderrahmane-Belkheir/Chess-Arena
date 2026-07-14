package org.Core.GameLogic.Services.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public final  class SpectateRequest extends GameEvent {
    private int userId;
    private String username;
    private String avatarUrl;
}