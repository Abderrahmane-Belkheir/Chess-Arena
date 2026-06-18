package org.Core.User.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSummary {
    private String id;
    private String username;
    private int  elo;
    String avatarUrl;
}
