package com.ous.aethererp.io;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {


    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerified;
    private String profilePictureUrl;
    private Set<RoleResponse> roles;

}


