package com.ous.aethererp.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
@Getter @Builder
@AllArgsConstructor
public class AuthResponse {

    private String email;
    private String name;
    private String token;
    private Boolean authenticated;

    private List<String> roles;
}
