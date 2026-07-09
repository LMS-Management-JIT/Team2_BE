package com.lms.backend.dto;

import com.lms.backend.dto.response.AccessTokenResponse;

public record LoginResult(AccessTokenResponse accessTokenResponse, String refreshTokenValue) {
}

