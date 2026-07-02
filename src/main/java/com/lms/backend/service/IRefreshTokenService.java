package com.lms.backend.service;

import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.RefreshTokenEntity;

public interface IRefreshTokenService {
    RefreshTokenEntity createRefreshToken(AccountEntity account, String userAgent);

    RefreshTokenEntity rotateRefreshToken(String tokenValue, String userAgent);

    void revokeAllTokens(AccountEntity account);
}

