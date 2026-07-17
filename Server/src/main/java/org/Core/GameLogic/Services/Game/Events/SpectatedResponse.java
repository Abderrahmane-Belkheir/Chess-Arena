package org.Core.GameLogic.Services.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public final class SpectatedResponse extends GameEvent {
    private int userId;
    private String username;
    private String avatarUrl;
    public SpectatedResponse(int userId,String username,String avatarUrl){
        this.userId=userId;
        this.username=username;
        this.avatarUrl=avatarUrl;
    }
}