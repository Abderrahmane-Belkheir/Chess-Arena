package org.Core.Game.Social;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChallengeRequestStore {

    private final Map<ChallengeRequest, String> pending = new ConcurrentHashMap<>();

    public boolean create(int challengerId, String targetUserId) {
        return pending.putIfAbsent(new ChallengeRequest(challengerId, Instant.now()), targetUserId) == null;
    }

    public String resolve(int challengerId) {
        return pending.remove(new ChallengeRequest(challengerId, Instant.now()));
    }

    /** Looks up who challengerId's pending challenge (if any) targets, without removing it. */
    public String peek(int challengerId) {
        return pending.get(new ChallengeRequest(challengerId, Instant.now()));
    }

    public void clearForTarget(String targetUserId) {
        pending.values().removeIf(targetUserId::equals);
    }


    @Getter
    public static class ChallengeRequest {
        private final int publicId;
        private final Instant createdAt;

        public ChallengeRequest(int publicId, Instant createdAt) {
            this.publicId = publicId;
            this.createdAt = createdAt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChallengeRequest other)) return false;
            return publicId == other.publicId; // identity is the challenger, not the timestamp
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(publicId);
        }
    }

}
