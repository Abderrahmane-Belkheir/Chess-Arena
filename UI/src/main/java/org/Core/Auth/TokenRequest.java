package org.Core.Auth;

import lombok.Builder;
import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class TokenRequest {
    private final String grantType;
    private final String clientId;
    private final String code;
    private final String redirectUri;
    private final String codeVerifier;
    private final String refreshToken;


    public String toUrlEncoded() {

        return buildParam("grant_type", grantType)
                + "&" + buildParam("client_id", clientId)
                + (code != null ? "&" + buildParam("code", code) : "")
                + (redirectUri != null ? "&" + buildParam("redirect_uri", redirectUri) : "")
                + (codeVerifier != null ? "&" + buildParam("code_verifier", codeVerifier) : "")
                + (refreshToken != null ? "&" + buildParam("refresh_token", refreshToken) : "");
    }

    private String buildParam(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "=" +
                URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
