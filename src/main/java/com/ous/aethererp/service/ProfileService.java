package com.ous.aethererp.service;

import com.ous.aethererp.io.ProfileRequest;
import com.ous.aethererp.io.ProfileResponse;

public interface ProfileService {


    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void resetPasswordOTP(String email);
    void resetPassword(String email, String otp, String newPassword);
    void sendOTP(String email);
    void verifyOTP(String email, String otp);
    String getLoggedInUserId(String email);
}
