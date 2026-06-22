package org.Core.Social;

import com.google.inject.Inject;
import org.Core.Shared.ApiClient;
import org.Core.Social.DTO.FriendsPage;
import org.Core.Social.DTO.InvitationsPage;
import org.Core.Social.DTO.UserSummary;
import org.Core.UI.LobbyScreens.Friends.PageResult;


import java.io.IOException;


public class FriendShipClient {

    private final ApiClient apiClient;
    private final String baseUrl="/api/v1/users";

    @Inject
    public FriendShipClient(ApiClient apiClient){
        this.apiClient=apiClient;
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

    public void deleteFriend(String userId){
        System.out.println("DELETING FRIEND "+userId);
    }

    public void unSend(int userId) throws IOException, InterruptedException {
      apiClient.DELETE(null,baseUrl+"/social/unSend?publicId="+userId,null);
    }

    public PageResult<FriendsPage.FriendEntry> fetchOnlineFriends(String cursor) throws IOException, InterruptedException {
       FriendsPage onlineFriendsPage= apiClient.GET(baseUrl+"/social/friends?cursor="+cursor, FriendsPage.class);
        return new PageResult<>(onlineFriendsPage.getFriends(),onlineFriendsPage.getNextCursor(),onlineFriendsPage.isHasMore());
    }

    public PageResult<FriendsPage.FriendEntry> fetchOfflineFriends(String cursor) throws IOException, InterruptedException {
        FriendsPage offlineFriendsPage=apiClient.GET(baseUrl+"/social/friends?cursor="+cursor,FriendsPage.class);
        return new PageResult<>(offlineFriendsPage.getFriends(),offlineFriendsPage.getNextCursor(),offlineFriendsPage.isHasMore());
    }

    public PageResult<InvitationsPage.InvitationEntry> fetchRequests(String cursor) throws IOException, InterruptedException {
       InvitationsPage invitationsPage= apiClient.GET(baseUrl+"/social/requests?cursor="+cursor, InvitationsPage.class);
        return new PageResult<>(invitationsPage.getInvitations(),invitationsPage.getNextCursor(),invitationsPage.isHasMore());
    }

}
