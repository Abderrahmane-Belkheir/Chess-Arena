package org.Core.Social.DTO;

import lombok.Data;
import org.Core.Config.DTO;

import java.util.List;
import java.util.Map;

@Data
public class FriendsList extends DTO {
    private Map<Status, List<FriendEntry>> friends;

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
