package com.ous.aethererp.service;


import com.ous.aethererp.RoleConstants;
import com.ous.aethererp.entity.RoleEntity;
import com.ous.aethererp.entity.UserEntity;
import com.ous.aethererp.exceptions.InvalidOTPException;
import com.ous.aethererp.io.ProfileRequest;
import com.ous.aethererp.io.ProfileResponse;
import com.ous.aethererp.io.RoleResponse;
import com.ous.aethererp.repo.RoleRepository;
import com.ous.aethererp.repo.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final UserEntityRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RoleRepository roleRepo;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists."
            );
        }

        UserEntity userEntity = convertToUserEntity(request);

        RoleEntity defaultRole = roleRepo.findByName(RoleConstants.USER)
                .orElseThrow(() ->
                        new RuntimeException("Default role ROLE_USER not found."));

        userEntity.getRoles().add(defaultRole);

        userEntity = userRepo.save(userEntity);

        return convertToProfileResponse(userEntity);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUserProfile = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
    return convertToProfileResponse(existingUserProfile);
    }

    @Override
    public void resetPasswordOTP(String email) {
        UserEntity existingUserByEmail = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found " + email));

        // Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(
                100000, 1000000));

        // Calculate expiry time (current time 15 mins in milliseconds)
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);

        // Update the profile / user
        existingUserByEmail.setResetPasswordOTP(otp);
        existingUserByEmail.setResetPasswordOTPExpiredAt(expiryTime);

        // Save the data into the database
        userRepo.save(existingUserByEmail);

        try{
            emailService.sendResetOTPEmail(existingUserByEmail.getEmail(), otp);
        } catch (Exception e){
            throw new RuntimeException("Unable to send email.");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
       UserEntity existingUser= userRepo.findByEmail(email)
               .orElseThrow(() -> new UsernameNotFoundException("User email not found: " + email));

       // Check if the otp is null or the existing otp is not equal to the provided one
        if (existingUser.getResetPasswordOTP() == null || !existingUser.getResetPasswordOTP().equals(otp)){
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getResetPasswordOTPExpiredAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP Expired.");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetPasswordOTP(null);
        existingUser.setResetPasswordOTPExpiredAt(0L);

        // resave user back to the database
        userRepo.save(existingUser);
    }

    @Override
    public void sendOTP(String email) {
        UserEntity existingUser = userRepo.findByEmail(email)
                .orElseThrow(()  -> new UsernameNotFoundException("User not found: "+ email));

        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()){
            return;
        }

        // Generate 6 digits otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(
                100000, 1000000));

        // Calculate expiry time (current time 15 mins in milliseconds)
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);

        // Update the profile / user
        existingUser.setVerifyOTP(otp);
        existingUser.setVerifyExpiredAt(expiryTime);

        // Save the data into the database
        userRepo.save(existingUser);

        try{
            emailService.sendOTPEmail(existingUser.getEmail(), otp);
        } catch (Exception e){
            throw new InvalidOTPException("Unable to send email ");
        }
    }

    @Override
    public void verifyOTP(String email, String otp) {

        UserEntity existingUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found: " + email));

        System.out.println("=================================");
        System.out.println("OTP VERIFICATION");
        System.out.println("Email: " + email);
        System.out.println("Submitted OTP: " + otp);
        System.out.println("Stored OTP: " + existingUser.getVerifyOTP());
        System.out.println("Stored Expiry: " + existingUser.getVerifyExpiredAt());
        System.out.println("Current Time: " + System.currentTimeMillis());
        System.out.println("=================================");

        // Check if an OTP exists
        if (existingUser.getVerifyOTP() == null) {
            throw new InvalidOTPException("No verification OTP found. Please request a new OTP.");
        }

        // Check if the OTP has expired
        if (existingUser.getVerifyExpiredAt() == null ||
                existingUser.getVerifyExpiredAt() <= System.currentTimeMillis()) {

            throw new InvalidOTPException("OTP has expired. Please request a new OTP.");
        }

        // Check if the OTP matches
        if (!existingUser.getVerifyOTP().equals(otp)) {

            System.out.println("OTP MISMATCH");
            System.out.println("Expected: " + existingUser.getVerifyOTP());
            System.out.println("Received: " + otp);

            throw new InvalidOTPException("Invalid OTP.");
        }

        // Verify account
        existingUser.setIsAccountVerified(true);

        // Clear OTP after successful verification
        existingUser.setVerifyOTP(null);

        // Clear OTP expiry
        existingUser.setVerifyExpiredAt(null);

        userRepo.save(existingUser);

        System.out.println("EMAIL VERIFIED SUCCESSFULLY");
    }

    @Override
    public String getLoggedInUserId(String email) {
       UserEntity existingUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Email Not Found"));

        return existingUser.getUserId();
    }

    // TODO: This method takes a database entity and convert it back into a restful response to reduce load and frequent requests on the database.
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        Set<RoleResponse> roles=newProfile.getRoles()
                .stream()
                .map(role -> RoleResponse.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .roleId(role.getRoleId())
                                .build())
                .collect(Collectors.toSet());
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .roles(roles)
                .build();
    }

    // TODO: This method takes a restful request and turn it back to a database entity
    private UserEntity convertToUserEntity(ProfileRequest request) {
         return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                 .isAccountVerified(false)
                 .resetPasswordOTPExpiredAt(0L)
                 .verifyOTP(null)
                 .verifyExpiredAt(0L)
                 .resetPasswordOTP(null)
                 .roles(new HashSet<>())
                 .build();
    }
}
