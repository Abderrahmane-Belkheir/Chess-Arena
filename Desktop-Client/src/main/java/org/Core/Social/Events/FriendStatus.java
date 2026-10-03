package org.Core.Social.Events;

import lombok.Data;
import org.Core.Social.DTO.FriendsList;

@Data
public final class FriendStatus extends SocialEvent{
    private int publicId;
    private FriendsList.Status newStatus;
    private int newElo;
}
