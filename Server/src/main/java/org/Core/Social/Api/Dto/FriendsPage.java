package org.Core.Social.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendsPage  {
    private List<FriendEntry> friends;
    private String nextCursor;
    private boolean hasMore;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FriendEntry{
       private String username;
       private   int    elo;
       private Status status;
       private String avatarUrl;
       private String avatarColor;
    }
    public enum Status{InGame,InLobby,Offline}
}