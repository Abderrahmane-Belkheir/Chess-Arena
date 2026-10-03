package org.Core.Social.Events;

import lombok.Data;

@Data
public final class InvitationRemoved extends SocialEvent{
    private int publicId;
}
