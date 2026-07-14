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


    public class PendingSpectateRequest {
        private final String requesterId;
        private final Instant createdAt;

        public PendingSpectateRequest(String requesterId, Instant createdAt) {
            this.requesterId = requesterId;
            this.createdAt = createdAt;
        }

        public String getRequesterId() { return requesterId; }
        public Instant getCreatedAt() { return createdAt; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PendingSpectateRequestStore.PendingSpectateRequest other)) return false;
            return requesterId.equals(other.requesterId); // identity is the requester, not the timestamp
        }

        @Override
        public int hashCode() {
            return requesterId.hashCode(); // must match equals — only requesterId contributes
        }
    }

}