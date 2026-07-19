package com.ous.aethererp.controller;

import com.ous.aethererp.io.RoleRequest;
import com.ous.aethererp.io.RoleResponse;
import com.ous.aethererp.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management")
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles
     */
    @Operation(summary = "Get all roles")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    /**
     * Create a role
     */
    @Operation(summary = "Create new role")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(
            @Valid @RequestBody RoleRequest request) {

        return roleService.createRole(request);
    }

    /**
     * Delete a role
     */
    @Operation(summary = "Delete role")
    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRole(
            @PathVariable Long roleId) {

        roleService.deleteRole(roleId);
    }

    /**
     * Assign roles to a user
     */
    @Operation(summary = "Assign roles to user")
    @PutMapping("/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void assignRolesToUser(

            @PathVariable Long userId,

            @RequestBody Set<String> roles) {

        roleService.assignRolesToUser(userId, roles);
    }

    /**
     * Remove a role from a user
     */
    @Operation(summary = "Remove role from user")
    @DeleteMapping("/{userId}/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void removeRoleFromUser(

            @PathVariable Long userId,

            @PathVariable String roleName) {

        roleService.removeRoleFromUser(userId, roleName);
    }

}
