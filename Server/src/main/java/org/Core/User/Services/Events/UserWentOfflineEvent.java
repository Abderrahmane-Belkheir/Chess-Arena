package org.Core.User.Services.Events;

/**
 * Published by PresenceStore.markOffline. Same reasoning as
 * UserCameOnlineEvent — keeps PresenceStore free of a direct dependency on
 * FriendStatusBroadcaster (which itself depends on PresenceStore).
 */
public record UserWentOfflineEvent(String userId, int publicId) {
}
