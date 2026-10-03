package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.Core.Social.Api.Dto.FriendsList;

@Getter
@AllArgsConstructor
public final class FriendStatus extends SocialEvent {
    private final int publicId;
    private final FriendsList.Status newStatus;
    private final int newElo;
}
