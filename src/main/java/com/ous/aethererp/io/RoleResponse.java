package com.ous.aethererp.io;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponse {

    private String name;
    private String roleId;
    private Long id;

    public RoleResponse(String name){
        this.name = name;
    }
}
