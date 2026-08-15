package com.ous.aethererp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter @Setter
@Builder
@Table(name = "tbl_users")
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;
    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    private String verifyOTP;
    private Boolean isAccountVerified;
    private Long verifyExpiredAt;
    private String resetPasswordOTP; // Password reset otp, use for forgot password
    private Long resetPasswordOTPExpiredAt; // Stores reset password OTP expiration timestamp.

    @CreationTimestamp
    @Column(updatable = false) // once created, it can never be changed, keeping track of the original creation date
    private Timestamp createdAt; // Automatically stores when a record is created.

    @UpdateTimestamp
    private Timestamp updatedAt; // Tracks the last modification time.

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tbl_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<RoleEntity> roles = new HashSet<>();

    public UserEntity() {}
}
