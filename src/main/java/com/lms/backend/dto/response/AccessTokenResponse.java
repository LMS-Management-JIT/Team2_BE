package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private long accessTokenTtlSeconds;
    private String username;
    private String role;
}

