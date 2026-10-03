package org.Core.Social.Events;

import lombok.Data;
import org.Core.Social.DTO.FriendsList;

@Data
public final class FriendAdded extends SocialEvent{
    private FriendsList.FriendEntry entry;
}
