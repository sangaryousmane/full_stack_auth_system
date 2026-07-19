package com.ous.aethererp.service;

import com.ous.aethererp.entity.RoleEntity;
import com.ous.aethererp.io.RoleRequest;
import com.ous.aethererp.io.RoleResponse;

import java.util.List;
import java.util.Set;

public interface RoleService {

    List<RoleResponse> getAllRoles();
    RoleResponse createRole(RoleRequest request);

    void deleteRole(Long roleId);
    void assignRolesToUser(Long userId, Set<String> roles);
    void removeRoleFromUser(Long userId, String roleName);
}
