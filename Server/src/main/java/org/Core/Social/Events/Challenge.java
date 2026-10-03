package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Published to a user when someone challenges them to a game — carries just
 * enough of the challenger's identity to render a "X wants to play you"
 * card without a round trip.
 */
@Getter
@AllArgsConstructor
public final class Challenge extends SocialEvent {
    private final int publicId;
    private final String username;
    private final int elo;
    private final String avatarUrl;
}
