package org.Core.User.Api.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummary {
    private int id;
    private String username;
    private int  elo;
    String avatarUrl;
    private Boolean isFriend;
    private InvitationStatus invitationStatus;



    public enum InvitationStatus{SENT,RECEIVED}
}
