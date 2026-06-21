package org.Core.Social.DTO;

import lombok.Data;
import org.Core.Shared.DTO;
@Data
public class UserSummary extends DTO {
    private int id;
    private String username;
    private int elo;
    private String avatarUrl;
    private Boolean isFriend;
    private  InvitationStatus invitationStatus;

    public  enum InvitationStatus{SENT,RECEIVED}
}
