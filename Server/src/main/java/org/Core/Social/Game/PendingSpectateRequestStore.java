package org.Core.Social.Game;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingSpectateRequestStore {

    private final Map<String, Set<PendingSpectateRequest>> pending = new ConcurrentHashMap<>();

    public boolean create(String requesterId, String targetUserId) {
        Set<PendingSpectateRequest> requests=pending.get(targetUserId);
        if(requests==null) return false;

        return requests.add(new PendingSpectateRequest(requesterId, Instant.now()));

    }

    public boolean resolve(String targetUserId, String requesterId) {
        Set<PendingSpectateRequest> requests = pending.get(targetUserId);
        if (requests == null) return false;
        return requests.removeIf(r -> r.getRequesterId().equals(requesterId));
    }

    public void clearForTarget(String targetUserId) {
        pending.remove(targetUserId);
    }
}