package org.Core.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthTokens {
    private String accessToken;
    private String refreshToken;
}
