package com.ous.aethererp.repo;

import com.ous.aethererp.entity.RefreshTokenEntity;
import com.ous.aethererp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);
    List<RefreshTokenEntity> findAllByUser(UserEntity user);
    void deleteByUser(UserEntity user);
    Optional<RefreshTokenEntity> findByUserAndRevokedFalse(UserEntity user);
}
