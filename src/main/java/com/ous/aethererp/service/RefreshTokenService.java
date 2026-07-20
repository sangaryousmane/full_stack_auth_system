package com.ous.aethererp.service;

import com.ous.aethererp.entity.RefreshTokenEntity;
import com.ous.aethererp.entity.UserEntity;

public interface RefreshTokenService {

    RefreshTokenEntity createRefreshToken(UserEntity user);
    RefreshTokenEntity verifyRefreshToken(String token);
    RefreshTokenEntity rotateRefreshToken(String oldToken);
    void revokeRefreshToken(String token);
    void revokeAllUserTokens(UserEntity user);

}
