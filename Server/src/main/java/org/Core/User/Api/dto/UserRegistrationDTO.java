package  org.Core.User.Api.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;
@Data
public class UserRegistrationDTO {
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