package org.Core.Game.Logic.Services.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class DrawOfferEvent extends GameEvent {
    private String userId;
}
