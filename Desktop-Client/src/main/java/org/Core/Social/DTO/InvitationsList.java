package org.Core.Social.DTO;

import lombok.Data;
import org.Core.Config.DTO;

import java.util.List;

@Data
public class InvitationsList extends DTO {
    private List<InvitationEntry> invitations;

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
