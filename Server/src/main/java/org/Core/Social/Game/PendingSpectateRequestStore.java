package org.Core.Social.Game;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingSpectateRequestStore {

    private final Map<String, Set<PendingSpectateRequest>> pending = new ConcurrentHashMap<>();

    public void init(String playerA,String playerB){
        pending.put(playerA,new HashSet<>());
        pending.put(playerB,new HashSet<>());
    }

    public boolean create(int spectatorId, String spectatedId) {

        Set<PendingSpectateRequest> requests=pending.get(spectatedId);
        if(requests==null) return false;

        return requests.add(new PendingSpectateRequest(spectatorId, Instant.now()));
    }

    public boolean resolve(int spectatorId,String spectatedId) {
        Set<PendingSpectateRequest> requests = pending.get(spectatedId);
        if (requests == null) return false;
        return requests.removeIf(r -> r.getRequesterId()== spectatorId);
    }

    public void clearForTarget(String targetUserId) {
        pending.remove(targetUserId);
    }


    @Getter
    public static class PendingSpectateRequest {
        private final int requesterId;
        private final Instant createdAt;

        public PendingSpectateRequest(int requesterId, Instant createdAt) {
            this.requesterId = requesterId;
            this.createdAt = createdAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PendingSpectateRequestStore.PendingSpectateRequest other)) return false;
            return requesterId==other.requesterId; // identity is the requester, not the timestamp
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(requesterId);
        }
    }

}