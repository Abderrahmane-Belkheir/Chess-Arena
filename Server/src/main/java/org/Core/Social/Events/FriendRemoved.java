package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Published when a friendship is deleted — publicId is the user who removed
 * the friendship. The receiving client should drop that user from its
 * friends list.
 */
@Getter
@AllArgsConstructor
public final class FriendRemoved extends SocialEvent {
    private final int publicId;
}
