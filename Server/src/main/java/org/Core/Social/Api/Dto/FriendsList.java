package org.Core.Social.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class FriendsList {

    private Map<Status,List<FriendEntry>> friends;

    @Getter
    public static class FriendEntry{
        private int id;
        private String username;
        private  int    elo;
        private String avatarUrl;
        private String avatarColor;
        private Status status;

        public FriendEntry(int id, String username, int elo, String avatarUrl, String avatarColor) {
            this(id, username, elo, avatarUrl, avatarColor, null);
        }

        public FriendEntry(int id, String username, int elo, String avatarUrl, String avatarColor, Status status) {
            this.id = id;
            this.username = username;
            this.elo = elo;
            this.avatarUrl = avatarUrl;
            this.avatarColor = avatarColor;
            this.status = status;
        }
    }

    public enum Status{InGame,InLobby,Offline}
}