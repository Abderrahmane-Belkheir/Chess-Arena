package org.Core.Auth;

import lombok.Data;

@Data
public class UserSession {
    private String id;
    private String username;
    private int elo;
    private String avatarUrl;
}
