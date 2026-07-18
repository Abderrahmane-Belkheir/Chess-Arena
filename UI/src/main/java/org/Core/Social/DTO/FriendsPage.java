package org.Core.Social.DTO;

import lombok.Data;
import org.Core.Config.DTO;


import java.util.List;
@Data
public class FriendsPage extends DTO {
    private List<FriendEntry> friends;
    private String nextCursor;
    private boolean hasMore;

    @Data
    public static class FriendEntry{
        private int id;
        private String username;
        private   int    elo;
        private Status status;
        private String avatarUrl;
        private String avatarColor;
    }
    public enum Status { InLobby, InGame ,Offline}
}
