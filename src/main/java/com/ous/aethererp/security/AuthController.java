package com.ous.aethererp.security;


import com.ous.aethererp.entity.RefreshTokenEntity;
import com.ous.aethererp.entity.RoleEntity;
import com.ous.aethererp.entity.UserEntity;
import com.ous.aethererp.io.*;
import com.ous.aethererp.repo.UserEntityRepository;
import com.ous.aethererp.service.EmailService;
import com.ous.aethererp.service.ProfileService;
import com.ous.aethererp.jwtUtils.JWTUtils;
import com.ous.aethererp.service.RefreshTokenServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final JWTUtils jwtUtils;
    private final ProfileService profileService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final UserEntityRepository userRepo;
    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse profile = profileService.createProfile(request);

        // Send email
        emailService.sendWelcomeEmail(profile.getEmail(), profile.getName());
        return profile;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());
            String accessToken = jwtUtils.generateToken(userDetails);
            ResponseCookie accessCookie = ResponseCookie.from("jwt", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .sameSite("Strict").build();


            UserEntity user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found."));
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);


            ResponseCookie refreshCookie =
                    ResponseCookie.from(
                                    "refresh_token",
                                    refreshToken.getToken())
                            .httpOnly(true)
                            .secure(false)
                            .sameSite("Strict")
                            .path("/")
                            .maxAge(Duration.ofDays(30))
                            .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(AuthResponse.builder()
                            .email(user.getEmail())
                            .name(user.getName())
                            .authenticated(true)
                            .roles(user.getRoles()
                                    .stream()
                                    .map(RoleEntity::getName)
                                    .toList()
                            ).build()
                    );

        } catch (BadCredentialsException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", true);
            err.put("message", "Email or password is incorrect.");
            return ResponseEntity.status(BAD_REQUEST).body(err);
        } catch (DisabledException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", true);
            err.put("message", "Account is disabled.");
            return ResponseEntity.status(UNAUTHORIZED).body(err);
        } catch (Exception ex) {
            log.error("LOGIN FAILED", ex);
            Map<String, Object> err = new HashMap<>();
            err.put("error", true);
            err.put("message", ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err);
        }
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            Authentication authentication) {
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return ResponseEntity.ok(isAuthenticated);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOTP(@RequestParam String email) {
        try {
            profileService.resetPasswordOTP(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> sendResetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("Passwords do not match.");
        }
        try {
            profileService.resetPassword(request.getEmail(),
                    request.getResetPasswordOTP(),
                    request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully.");

        } catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("isAuthenticated()")
    @PostMapping("/send-otp")
    public void sendVerifyOTP(@CurrentSecurityContext(expression = "authentication?.name") String email) {

        try {
            profileService.sendOTP(email);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> verifyEmail(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User is not authenticated."));
        }

        String email = authentication.getName();

        String otp = request.get("otp") != null
                ? request.get("otp").toString().trim()
                : null;

        if (otp == null || !otp.matches("\\d{6}")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "A valid 6-digit OTP is required."));
        }

        profileService.verifyOTP(email, otp);

        return ResponseEntity.ok(
                Map.of("message", "Email verified successfully.")
        );
    }


    private void authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        log.info("Logging out now.......");
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        log.info("Logging out successful!!");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if ("refresh_token".equals(cookie.getName())) {

                    refreshToken = cookie.getValue();

                    break;
                }
            }
        }

        if (refreshToken == null ||
                refreshToken.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token missing.");
        }

        try {

            RefreshTokenEntity token =
                    refreshTokenService
                            .rotateRefreshToken(refreshToken);

            UserDetails userDetails =
                    appUserDetailService
                            .loadUserByUsername(
                                    token.getUser().getEmail()
                            );

            String newAccessToken =
                    jwtUtils.generateToken(userDetails);

            addAccessCookie(
                    response,
                    newAccessToken
            );

            addRefreshCookie(
                    response,
                    token.getToken()
            );

            return ResponseEntity.ok(
                    "Token refreshed successfully."
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ex.getMessage());
        }
    }

    private void addRefreshCookie(
            HttpServletResponse response,
            String token) {

        ResponseCookie cookie =
                ResponseCookie.from("refresh_token", token)
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Strict")
                        .path("/")
                        .maxAge(30 * 24 * 60 * 60)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }

    private void addAccessCookie(
            HttpServletResponse response,
            String token) {

        ResponseCookie cookie =
                ResponseCookie.from("jwt", token)
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Strict")
                        .path("/")
                        .maxAge(15 * 60)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }

}

