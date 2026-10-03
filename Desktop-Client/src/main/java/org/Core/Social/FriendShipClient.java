package org.Core.Social;

import com.google.inject.Inject;
import org.Core.Config.ApiClient;
import org.Core.Game.Events.SpectatorResponse;
import org.Core.Realtime.RealtimeGateway;
import org.Core.Social.DTO.FriendsList;
import org.Core.Social.DTO.InvitationsList;
import org.Core.Social.DTO.UserSummary;


import java.io.IOException;
import java.util.List;


public class FriendShipClient {

    private final ApiClient apiClient;
    private final String baseUrl="/api/v1/users";
    private final RealtimeGateway realtimeGateway;

    // Cached result of the single /social/friends call — the server now
    // returns online and offline friends together (grouped by status) in
    // one response, so the first caller (Online, loaded eagerly) fetches it
    // and Offline just reads from the cache instead of firing a second
    // request.
    private FriendsList friendsCache;

    // Static bridge to this Guice singleton, same pattern as
    // GameRealtimeGatewayStub.getSession() — lets static-only utilities like
    // GameActions reach it without needing their own DI wiring.
    private static FriendShipClient instance;

    @Inject
    public FriendShipClient(ApiClient apiClient,RealtimeGateway realtimeGateway){
        this.apiClient=apiClient;
        this.realtimeGateway=realtimeGateway;
        instance = this;
    }

    public static FriendShipClient getInstance() { return instance; }

    public void challenge(int userId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/challenge/request?publicId="+userId,null);
    }

    public void acceptChallenge(int userId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/challenge/accept?publicId="+userId,null);
    }

    public void rejectChallenge(int userId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/challenge/reject?publicId="+userId,null);
    }

    public void spectate(int userId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/spectate/request?userId="+userId,null);
        realtimeGateway.subscribe("/user/queue/spectate.responses", SpectatorResponse.class);
    }

    public void acceptSpectate(int spectatorId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/spectate/accept?spectatorId="+spectatorId,null);
    }

    public void rejectSpectate(int spectatorId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/spectate/reject?spectatorId="+spectatorId,null);
    }

    public void quitSpectating(int targetId) throws IOException, InterruptedException {
        apiClient.POST(null,"/api/v1/game/spectate/quit?targetId="+targetId,null);
    }

    public UserSummary search(int userId) throws IOException, InterruptedException {
        return apiClient.GET(baseUrl+"/search?publicId="+userId, UserSummary.class);
    }

    public void invite(int userId) throws IOException, InterruptedException {
        apiClient.POST(null,baseUrl+"/social/invite?publicId="+userId,null);
    }

    public void accept(int userId) throws IOException, InterruptedException {
      apiClient.PUT(null,baseUrl+"/social/accept?publicId="+userId,null);
    }

    public void reject(int userId) throws IOException, InterruptedException {
        apiClient.PUT(null,baseUrl+"/social/reject?publicId="+userId,null);
    }

    public void deleteFriend(int userId) throws IOException, InterruptedException {
        apiClient.DELETE(null,baseUrl+"/social/delete?publicId="+userId,null);
    }

    public void unSend(int userId) throws IOException, InterruptedException {
      apiClient.DELETE(null,baseUrl+"/social/unSend?publicId="+userId,null);
    }

    // No pagination — the server returns the whole list (all three statuses
    // grouped together) in one shot, so this is only ever called once per
    // session; whichever tab loads first triggers the request and the other
    // two just read from the cached result.

    private synchronized FriendsList fetchFriendsList() throws IOException, InterruptedException {
        if (friendsCache == null) {
            friendsCache = apiClient.GET(baseUrl + "/social/friends", FriendsList.class);
            friendsCache.getFriends().forEach((status, entries) ->
                    entries.forEach(entry -> entry.setStatus(status)));
        }
        return friendsCache;
    }

    private List<FriendsList.FriendEntry> fetchByStatus(FriendsList.Status status) throws IOException, InterruptedException {
        return fetchFriendsList().getFriends().getOrDefault(status, List.of());
    }

    public List<FriendsList.FriendEntry> fetchInLobbyFriends() throws IOException, InterruptedException {
        return fetchByStatus(FriendsList.Status.InLobby);
    }

    public List<FriendsList.FriendEntry> fetchInGameFriends() throws IOException, InterruptedException {
        return fetchByStatus(FriendsList.Status.InGame);
    }

    public List<FriendsList.FriendEntry> fetchOfflineFriends() throws IOException, InterruptedException {
        return fetchByStatus(FriendsList.Status.Offline);
    }

    public List<InvitationsList.InvitationEntry> fetchInvitations() throws IOException, InterruptedException {
        return apiClient.GET(baseUrl+"/social/invitations", InvitationsList.class).getInvitations();
    }

}
