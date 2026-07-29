package com.ous.aethererp.controller;

import com.ous.aethererp.config.OpenApiConfig;
import com.ous.aethererp.io.ProfileRequest;
import com.ous.aethererp.io.ProfileResponse;
import com.ous.aethererp.io.UpdateProfileRequest;
import com.ous.aethererp.service.EmailService;
import com.ous.aethererp.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequestMapping("/profile")
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        ProfileResponse profile = profileService.createProfile(request);

        // Send email
        emailService.sendWelcomeEmail(profile.getEmail(), profile.getName());
        return profile;
    }

    @GetMapping
    public ProfileResponse getProfileDetails(
            @CurrentSecurityContext(expression = "authentication?.name") String email){
    return profileService.getProfile(email);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @CurrentSecurityContext(expression = "authentication?.name") String email,
            @Valid @RequestBody UpdateProfileRequest request){
        return ResponseEntity.ok(profileService.updateProfile(email, request));
    }

    @PostMapping(value = "/picture", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> uploadProfilePicture(
            @CurrentSecurityContext(expression = "authentication?.name") String email,
            @RequestParam("profilePicture") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadProfilePicture(email, file));
    }

    @DeleteMapping("/picture")
    public ResponseEntity<ProfileResponse> removeProfilePicture(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        return ResponseEntity.ok(profileService.removeProfilePicture(email));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutFromAllDevices(
            @CurrentSecurityContext(expression = "authentication?.name") String email){
        profileService.logoutFromAllDevices(email);
        return ResponseEntity.ok(Map.of(
                "message",
                "All active sessions have been revoked."));
    }

    public ResponseEntity<?> deleteAccount(
            @CurrentSecurityContext(expression = "authentication?.name") String email){
        profileService.deleteAccount(email);
        return ResponseEntity.ok(Map.of(
                "message",
                "Account deleted successfully."));
    }

}

// pwd: 83180043
// pwd: mulbah3030
