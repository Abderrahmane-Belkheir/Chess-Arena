package org.Core.Social.Events;

import lombok.Data;
import org.Core.Social.DTO.InvitationsList;

@Data
public final class Invitation extends SocialEvent{
    private InvitationsList.InvitationEntry entry;
}
