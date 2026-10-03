package org.Core.Social.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.Core.Social.Api.Dto.InvitationsList;

@Getter
@AllArgsConstructor
public final class Invitation extends SocialEvent {
    private final InvitationsList.InvitationEntry entry;
}
