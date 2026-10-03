package org.Core.Social.Events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FriendStatus.class,      name = "FRIEND_STATUS"),
        @JsonSubTypes.Type(value = Invitation.class,         name = "INVITATION"),
        @JsonSubTypes.Type(value = InvitationRemoved.class, name = "INVITATION_REMOVED"),
        @JsonSubTypes.Type(value = FriendRemoved.class,     name = "FRIEND_REMOVED"),
        @JsonSubTypes.Type(value = FriendAdded.class,       name = "FRIEND_ADDED"),
        @JsonSubTypes.Type(value = Challenge.class,         name = "CHALLENGE")
})
public sealed abstract class SocialEvent permits Invitation , FriendStatus, InvitationRemoved, FriendRemoved, FriendAdded, Challenge {
}
