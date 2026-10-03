package org.Core.Social.Events;

import lombok.Data;

@Data
public final class Challenge extends SocialEvent{
    private int publicId;
    private String username;
    private int elo;
    private String avatarUrl;
}
