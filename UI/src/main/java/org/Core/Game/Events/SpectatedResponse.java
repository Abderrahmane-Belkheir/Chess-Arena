package org.Core.Game.Events;

import lombok.Data;

@Data
public final  class SpectatedResponse extends GameEvent {
    private int userId;
    private String username;
    private String avatarUrl;
}
