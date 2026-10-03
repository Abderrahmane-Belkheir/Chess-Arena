package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.Core.Social.Api.Dto.FriendsList;

/**
 * Published to the original sender of a friend request when the recipient
 * accepts it — the sender's client should insert entry into whichever
 * section matches entry.getStatus().
 */
@Getter
@AllArgsConstructor
public final class FriendAdded extends SocialEvent {
    private final FriendsList.FriendEntry entry;
}
