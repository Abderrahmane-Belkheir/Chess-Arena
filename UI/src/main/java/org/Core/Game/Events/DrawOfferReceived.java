package org.Core.Game.Events;

import lombok.Data;

@Data
public final  class DrawOfferReceived extends GameEvent{
    private String userId;
}
