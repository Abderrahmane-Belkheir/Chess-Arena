package org.Core.Social.DTO;

import lombok.Data;
import org.Core.Shared.DTO;

import java.util.List;
@Data
public class InvitationsPage extends DTO {
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