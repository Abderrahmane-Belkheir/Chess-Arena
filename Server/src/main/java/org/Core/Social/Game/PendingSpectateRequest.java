package org.Core.Social.Game;

import java.time.Instant;

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
        if (!(o instanceof PendingSpectateRequest other)) return false;
        return requesterId.equals(other.requesterId); // identity is the requester, not the timestamp
    }

    @Override
    public int hashCode() {
        return requesterId.hashCode(); // must match equals — only requesterId contributes
    }
}
