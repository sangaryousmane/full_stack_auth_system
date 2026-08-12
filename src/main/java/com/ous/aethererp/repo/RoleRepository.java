package com.ous.aethererp.repo;

import com.ous.aethererp.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);
    Optional<RoleEntity> findByRoleId(Long roleId);

    @Modifying
    @Query(value =
            """
                DELETE FROM tbl_user_roles
                WHERE user_id = :userId
                """,
            nativeQuery = true
    )
    void deleteUserRoles(
            @Param("userId") Long userId
    );
}
