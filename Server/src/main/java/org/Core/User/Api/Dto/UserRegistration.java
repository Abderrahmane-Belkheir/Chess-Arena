package  org.Core.User.Api.Dto;

import lombok.Data;

@Data
public class UserRegistration {
    private String userId;
    private String email;
    private String name;
    private String createdAt;
}