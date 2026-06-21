package org.Core.Social.Api.Dto;

import lombok.Data;

import java.util.List;

@Data
public class InvitationsPage {
    private List<InvitationEntry> invitations;
    private String nextCursor;
    private boolean hasMore;
    @Data
    public static class InvitationEntry{
        private String username;
        private String publicId;
        private int elo;
        private String avatarUrl;
        private String avatarColor;
        private boolean incoming;

}
}