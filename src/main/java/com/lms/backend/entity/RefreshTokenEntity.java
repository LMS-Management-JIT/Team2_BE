package com.lms.backend.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {
    @DocumentId
    private String id;

    private String token;

    @PropertyName("account")
    private AccountEntity account;

    private Instant expiresAt;
    private boolean revoked;
    private String userAgent;
}

