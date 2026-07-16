package com.ous.aethererp.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Builder
@AllArgsConstructor
@Entity
@Table(name = "tbl_roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true)
    private String roleId;
}
