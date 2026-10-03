package org.Core.Social.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InvitationsList {
    private List<InvitationEntry> invitations;
    @Getter
    @AllArgsConstructor
    public static class InvitationEntry{
        private String username;
        private String publicId;
        private int elo;
        private String avatarUrl;
        private String avatarColor;
        private boolean incoming;

}
}