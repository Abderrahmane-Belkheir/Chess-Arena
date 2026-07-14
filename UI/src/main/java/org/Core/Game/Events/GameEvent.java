package org.Core.Game.Events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpponentMove.class,    name = "MOVE"),
        @JsonSubTypes.Type(value = GameOverInfo.class,        name = "GAME_OVER"),
        @JsonSubTypes.Type(value = MoveConfirmation.class,     name = "MOVE_CONFIRM"),
        @JsonSubTypes.Type(value = DrawOfferReceived.class,     name = "DRAW_OFFERED"),
        @JsonSubTypes.Type(value = SpectatedResponse.class,name = "SPECTATE_REQUEST")
})
public sealed abstract class GameEvent
        permits DrawOfferReceived, GameOverInfo, MoveConfirmation, OpponentMove, SpectatedResponse {
}