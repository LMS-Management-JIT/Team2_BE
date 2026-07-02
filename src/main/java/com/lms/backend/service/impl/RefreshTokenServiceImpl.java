package com.lms.backend.service.impl;

import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.RefreshTokenEntity;
import com.lms.backend.exception.InvalidTokenException;
import com.lms.backend.repository.RefreshTokenRepository;
import com.lms.backend.service.IRefreshTokenService;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements IRefreshTokenService {
    private static final Logger log = Logger.getLogger(RefreshTokenServiceImpl.class.getName());
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.expiration}")
    private long refreshExpiration;

    @Override
    public RefreshTokenEntity createRefreshToken(AccountEntity account, String userAgent) {
        Instant now = Instant.now();
        long activeSessions = refreshTokenRepository.countActiveByUser(account, now);
        if (activeSessions >= 5) {
            log.warning("Active refresh sessions >= 5 for user " + account.getEmail() + ", revoking all.");
            refreshTokenRepository.revokeAllByUser(account);
        }

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .account(account)
                .expiresAt(now.plusMillis(refreshExpiration))
                .revoked(false)
                .userAgent(userAgent)
                .build();

        return refreshTokenRepository.save(refreshTokenEntity);
    }

    @Override
    public RefreshTokenEntity rotateRefreshToken(String tokenValue, String userAgent) {
        RefreshTokenEntity existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("not found"));

        if (existing.isRevoked()) {
            log.severe("SECURITY: Refresh token REUSE detected for user " + existing.getAccount().getEmail() + "!");
            refreshTokenRepository.revokeAllByUser(existing.getAccount());
            throw new InvalidTokenException("Token reuse detected - all sessions invalidated");
        }

        Instant now = Instant.now();
        if (!existing.getExpiresAt().isAfter(now)) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new InvalidTokenException("expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getAccount(), userAgent);
    }

    @Override
    public void revokeAllTokens(AccountEntity account) {
        refreshTokenRepository.revokeAllByUser(account);
        log.info("Revoked all refresh tokens for: " + account.getEmail());
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up expired/revoked refresh tokens");
    }
}
