package org.Core.GameLogic.Services.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class DrawOfferEvent extends GameEvent {
    private String userId;
}
