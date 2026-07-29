package com.ous.aethererp.service;

import com.ous.aethererp.io.ProfileRequest;
import com.ous.aethererp.io.ProfileResponse;
import com.ous.aethererp.io.ResetPasswordRequest;
import com.ous.aethererp.io.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {


    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void resetPasswordOTP(String email);
    void resetPassword(String email, String otp, String newPassword);
    void sendOTP(String email);
    void verifyOTP(String email, String otp);
    String getLoggedInUserId(String email);
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);
    ProfileResponse uploadProfilePicture(String email, MultipartFile file);
    ProfileResponse removeProfilePicture(String email);
    void deleteAccount(String email);
    void logoutFromAllDevices(String email);
    void changePassword(String email, ResetPasswordRequest request);
}
