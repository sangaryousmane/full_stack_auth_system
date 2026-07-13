package com.ous.aethererp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;


@Entity
@Data
@Builder
@RequiredArgsConstructor
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
}
