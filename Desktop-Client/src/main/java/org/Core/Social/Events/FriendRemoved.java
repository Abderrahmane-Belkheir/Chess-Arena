package org.Core.Social.Events;

import lombok.Data;

@Data
public final class FriendRemoved extends SocialEvent{
    private int publicId;
}
