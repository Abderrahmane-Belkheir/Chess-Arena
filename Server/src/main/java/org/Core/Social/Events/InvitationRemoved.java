package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Published when a pending request between the receiving user and publicId
 * goes away — either the recipient declined it, or the sender unsent it.
 * The receiving client should remove the entry matching publicId from its
 * Requests list, regardless of which side it was on.
 */
@Getter
@AllArgsConstructor
public final class InvitationRemoved extends SocialEvent {
    private final int publicId;
}
