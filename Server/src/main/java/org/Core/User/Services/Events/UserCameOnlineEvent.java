package org.Core.User.Services.Events;

/**
 * Published by PresenceStore.markOnline on an offline->online transition.
 * Exists to break the circular dependency PresenceStore <-> FriendStatusBroadcaster
 * (the broadcaster needs PresenceStore.groupByPresence, so PresenceStore can't
 * depend on the broadcaster directly) — FriendStatusBroadcaster listens for
 * this instead of being called from PresenceStore.
 */
public record UserCameOnlineEvent(String userId, int publicId) {
}
