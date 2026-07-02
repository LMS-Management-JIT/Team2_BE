package com.lms.backend.repository;

import com.lms.backend.entity.AccountEntity;
import com.lms.backend.entity.RefreshTokenEntity;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    public RefreshTokenEntity save(RefreshTokenEntity refreshTokenEntity) {
        return refreshTokenEntity;
    }

    public Optional<RefreshTokenEntity> findByToken(String tokenValue) {
        return Optional.empty();
    }

    public long countActiveByUser(AccountEntity account, Instant now) {
        return 0L;
    }

    public void revokeAllByUser(AccountEntity account) {
        // no-op placeholder for Firestore-backed implementation
    }

    public void deleteExpiredAndRevoked(Instant now) {
        // no-op placeholder for Firestore-backed implementation
    }

    public String update(String tokenId, Map<String, Object> updates) {
        return "Update thành công";
    }
}

