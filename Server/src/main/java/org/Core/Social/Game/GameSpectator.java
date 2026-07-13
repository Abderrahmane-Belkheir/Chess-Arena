package org.Core.Social.Game;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Persistence.FriendShipRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSpectator {

    private final FriendShipRepo friendShipRepo;
    private final SpectatorApprovalRegistry approvalRegistry;
    private final PendingSpectateRequestStore spectateRequestStore;
    private final ApplicationEventPublisher eventPublisher;


    public void requestSpectate(String targetId,String spectatorId){
        if(approvalRegistry.isApproved(targetId,spectatorId)) return;
        if(spectateRequestStore.create(spectatorId,targetId)) return;
        eventPublisher.publishEvent(null);
    }

    public void acceptSpectate(String userId,String spectatorId){
        if(approvalRegistry.isApproved(userId,spectatorId)) return;
//        if(!spectateRequestStore.resolve()) return;
        approvalRegistry.approve(userId,spectatorId);
        eventPublisher.publishEvent(null);
    }

    public boolean isApproved(String targetId,String spectatorId){
        return approvalRegistry.isApproved(targetId,spectatorId);
    }

}
