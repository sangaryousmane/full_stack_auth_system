package com.ous.aethererp.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tbl_refresh_tokens")
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  Random refresh token
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     *  Token expiration time
     */
    @Column(nullable = false)
    private Instant expiryDate;

    /**
     *  Whether this token has been revoked
     */
    @Column(nullable = false)
    private Boolean isTokenRevoked;


    /**
     *  Token creation time
     */
    private Long createdAt;

    /**
     *  Last usage of the token
     */
    private Long lastUsedAt;


    /**
     * Show Logged in devices

     * ✓ Chrome - Windows
     * ✓ Safari - iPhone
     * ✓ Edge - Office Laptop
     */
    private String deviceName;

    /**
     *  Track the device IP address
     */
    private String ipAddress;

    /**
     * The user agent
     */
    private String userAgent;

    /**
     *  Many refresh tokens can belong to one user.
     *  E.g. One user login through multiple devices.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
