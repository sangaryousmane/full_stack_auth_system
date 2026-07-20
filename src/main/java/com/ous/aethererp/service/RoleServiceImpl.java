package com.ous.aethererp.service;
import com.ous.aethererp.entity.RoleEntity;
import com.ous.aethererp.entity.UserEntity;
import com.ous.aethererp.exceptions.RoleNotFoundException;
import com.ous.aethererp.io.RoleRequest;
import com.ous.aethererp.io.RoleResponse;
import com.ous.aethererp.repo.RoleRepository;
import com.ous.aethererp.repo.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;

@Log
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepo;
    private final UserEntityRepository userRepo;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepo.findAll()
                .stream()
                .map(this::convertToRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.info("User Role with ID "+ request.getId()+" Created Successfully.");

        RoleEntity role = RoleEntity.builder()
                        .roleId(UUID.randomUUID().toString())
                        .name(request.getName())
                        .build();
        roleRepo.save(role);
        log.info("User Role with ID "+ role.getId()+" Created Successfully.");
        return convertToRoleResponse(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        RoleEntity roleById = roleRepo.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role with ID" + roleId + " not found."));
        if(roleById.getName().equals("ROLE_ADMIN")){
            throw new RoleNotFoundException(
                    "Cannot delete default role");
        }
        roleRepo.deleteById(roleById.getId());
        log.warning("User Role with ID "+ roleById.getId()+" Deleted Successfully.");

    }

    @Override
    public void assignRolesToUser(Long userId, Set<String> roleNames) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found."));
        Set<RoleEntity> roles=new HashSet<>();

        for (String roleName: roleNames){
            RoleEntity role=roleRepo.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException(roleName + "doesn't exist."));

            log.info("Role " + role.getName()+
                    " with User ID "+ user.getId()+" has been assign Successfully.");

            roles.add(role);
        }

        user.getRoles().addAll(roles);
        userRepo.save(user);

    }

    @Override
    public void removeRoleFromUser(Long userId, String roleName) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.getRoles()
                .removeIf(role -> role.getName().equalsIgnoreCase(roleName));
        userRepo.save(user);
        log.warning("User Role with User ID "+ user.getId()+" has been remove Successfully.");
    }

    private RoleResponse convertToRoleResponse(RoleEntity role) {
        return RoleResponse.builder()
                .name(role.getName())
                .id(role.getId())
                .build();
    }
}
