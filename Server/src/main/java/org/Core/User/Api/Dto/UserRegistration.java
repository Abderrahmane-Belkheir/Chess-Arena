package  org.Core.User.Api.Dto;

import lombok.Data;

@Data
public class UserRegistration {
    private String userId;
    private String email;
    private String name;
    private String createdAt;

    @java.lang.Override
    public java.lang.String toString() {
        return "Auth0UserRegistrationDTO{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}