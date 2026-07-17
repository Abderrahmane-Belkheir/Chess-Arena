package org.Core.Social.Game;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class SpectatorApprovalRegistry {

    private final Map<Integer, Set<String>> approvals = new ConcurrentHashMap<>();

    public void init(int playerA,int playerB){
        approvals.put(playerA,new HashSet<>());
        approvals.put(playerB,new HashSet<>());
    }

    public boolean approve(int targetUserId, String spectatorUserId) {
        Set<String> approved=approvals.get(targetUserId);
        return approved != null && approved.add(spectatorUserId);
    }

    public boolean isApproved(int targetUserId, String spectatorUserId) {
        Set<String> approved = approvals.get(targetUserId);
        return approved != null && approved.contains(spectatorUserId);
    }

    public void revoke(String targetUserId, String spectatorUserId) {
        Set<String> approved = approvals.get(targetUserId);
        if (approved != null) {
            approved.remove(spectatorUserId);
        }
    }


    public Set<String> getApprovedSpectators(String targetUserId) {
        Set<String> approved = approvals.get(targetUserId);
        return approved == null ? Collections.emptySet() : Collections.unmodifiableSet(approved);
    }


    public void clearForTarget(String targetUserId) {
        approvals.remove(targetUserId);
    }
}