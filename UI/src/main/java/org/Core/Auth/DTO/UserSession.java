package org.Core.Auth.DTO;

import lombok.Data;
import org.Core.Shared.DTO;

@Data
public class UserSession extends DTO  {
    private String id;
    private String username;
    private int elo;
    private String avatarUrl;

}
