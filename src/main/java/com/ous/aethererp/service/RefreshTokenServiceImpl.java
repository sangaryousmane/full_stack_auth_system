package com.ous.aethererp.service;

import com.ous.aethererp.entity.RefreshTokenEntity;
import com.ous.aethererp.entity.UserEntity;
import com.ous.aethererp.jwtUtils.JWTUtils;
import com.ous.aethererp.repo.RefreshTokenRepository;
import com.ous.aethererp.repo.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;
    private final UserEntityRepository userRepo;
    private final JWTUtils jwtUtils;

    /**
     * Refresh token validity (30 days)
     */
    private static final long REFRESH_TOKEN_VALIDITY = 30L * 24 * 60 * 60 * 1000;

    /**
     * Secure random generator
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure refresh token.
     */
    private String generateRefreshToken() {
        byte[] randomBytes = new byte[64];

        secureRandom.nextBytes(randomBytes);

        return Base64.getEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    /**
     * Creates a refresh token for a user.
     */
    @Override
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        String token = generateRefreshToken();

        RefreshTokenEntity refreshToken=
                RefreshTokenEntity.builder()
                        .token(token)
                        .expiryDate(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY)
                        .revoked(false)
                        .user(user)
                        .build();

        refreshTokenRepo.save(refreshToken);

        log.info("Refresh token created for user: {}", user.getEmail());

        return refreshToken;
    }

    /**
     * Verifies whether a refresh token is still valid.
     */
    @Override
    public RefreshTokenEntity verifyRefreshToken(String token) {
        RefreshTokenEntity refreshToken=
                refreshTokenRepo.findByToken(token)
                        .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()){
            throw new RuntimeException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiryDate() < System.currentTimeMillis()){
            refreshTokenRepo.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired.");
        }
        return refreshToken;
    }

    /**
     * Rotates a refresh token.
     * Old refresh token is revoked and a completely new one is generated.
     */
    @Override
    public RefreshTokenEntity rotateRefreshToken(String oldToken) {
        RefreshTokenEntity existing = verifyRefreshToken(oldToken);

        existing.setRevoked(true);

        refreshTokenRepo.save(existing);

        log.info("Refresh token rotated for user : {}", existing.getUser().getEmail());

        return createRefreshToken(existing.getUser());
    }

    /**
     * Revokes a single refresh token.
     */
    @Override
    public void revokeRefreshToken(String token) {
        refreshTokenRepo.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepo.save(refreshToken);
                    log.info("Refresh token revoked.");
                });
    }

    /**
     * Revokes every refresh token
     * belonging to a user.
     */
    @Override
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepo.findAllByUser(user)
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                });

        log.info("All refresh tokens revoked for {}", user.getEmail());


    }
}
