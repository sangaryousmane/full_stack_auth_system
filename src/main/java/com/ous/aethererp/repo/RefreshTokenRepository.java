package com.ous.aethererp.repo;

import com.ous.aethererp.entity.RefreshTokenEntity;
import com.ous.aethererp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);
    List<RefreshTokenEntity> findAllByUser(UserEntity user);
    void deleteByUser(UserEntity user);
    Optional<RefreshTokenEntity> findByUserAndRevokedFalse(UserEntity user);

    void deleteAllByUser(UserEntity user);

    @Modifying
    @Query ("""
            UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.user = :user
            """)
    void revokeAllByUser(@Param("user") UserEntity user);
}
