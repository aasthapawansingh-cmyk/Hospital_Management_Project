package com.example.hosptial.auth;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RefreshTokenService {
     private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDurationMs = 1000 * 60 * 60 * 24 * 7; // 7 days

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username); // optional: one token per user

        RefreshToken refreshToken = new RefreshToken(
            UUID.randomUUID().toString(),
            Instant.now().plusMillis(refreshTokenDurationMs),
            username
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        return token;
    }
}
