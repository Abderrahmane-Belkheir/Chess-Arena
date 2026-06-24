package org.Core.Auth.DTO;

import lombok.Data;
import org.Core.Config.DTO;

@Data
public class UserSession extends DTO  {
    private int id;
    private String username;
    private int elo;
    private String avatarUrl;
}
