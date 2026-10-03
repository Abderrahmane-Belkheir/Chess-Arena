package org.Core.User.Services;

import lombok.RequiredArgsConstructor;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.Core.User.Services.Events.UserCameOnlineEvent;
import org.Core.User.Services.Events.UserWentOfflineEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceStore {

    private static final String PREFIX = "online:";
    private static final Duration TTL = Duration.ofSeconds(45);

    // Publishes UserCameOnlineEvent instead of calling FriendStatusBroadcaster
    // directly — the broadcaster depends on PresenceStore (groupByPresence),
    // so a direct dependency back would be circular.
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepo userRepo;
    private final RedisTemplate<String, String> redis;

    public void markOnline(String userId) {
        if (!isOnline(userId)) {
            userRepo.findById(userId).ifPresent(user ->
                    eventPublisher.publishEvent(new UserCameOnlineEvent(userId, user.getPublicId())));
        }
        redis.opsForValue().set(PREFIX + userId, "1", TTL);
    }

    /**
     * Removes the user's presence key immediately (rather than waiting on the
     * TTL), marks their DB status OFFLINE, and notifies their friends.
     */
    public void markOffline(String userId) {
        redis.delete(PREFIX + userId);
        userRepo.findById(userId).ifPresent(user -> {
            userRepo.updateUserStatus(User.Status.OFFLINE, userId);
            eventPublisher.publishEvent(new UserWentOfflineEvent(userId, user.getPublicId()));
        });
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + userId));
    }

    /**
     * Groups the given user IDs into online/offline in a single round trip (MGET).
     * Key true -> online user IDs, key false -> offline user IDs; both are always present.
     */
    public Map<Boolean, List<String>> groupByPresence(List<String> userIds) {
        Map<Boolean, List<String>> grouped = new HashMap<>();
        grouped.put(true, new ArrayList<>());
        grouped.put(false, new ArrayList<>());

        if (userIds.isEmpty()) return grouped;

        List<String> keys = userIds.stream().map(id -> PREFIX + id).toList();
        List<String> values = redis.opsForValue().multiGet(keys);

        for (int i = 0; i < userIds.size(); i++) {
            boolean online = values != null && values.get(i) != null;
            grouped.get(online).add(userIds.get(i));
        }

        return grouped;
    }

}
