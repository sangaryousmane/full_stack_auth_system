package com.ous.aethererp.io;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "New Password is required.")
    private String newPassword;

    @NotBlank(message = "New OTP is required.")
    private String resetPasswordOTP;

    @Email @NotBlank(message = "Email is required.")
    private String email;
}
