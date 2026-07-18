package com.ous.aethererp.security;


import com.ous.aethererp.io.AuthRequest;
import com.ous.aethererp.io.AuthResponse;
import com.ous.aethererp.io.ResetPasswordRequest;
import com.ous.aethererp.service.ProfileService;
import com.ous.aethererp.jwtUtils.JWTUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;


@RestController
@RequiredArgsConstructor
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final JWTUtils jwtUtils;
    private final ProfileService profileService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());
            String jtwToken = jwtUtils.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jtwToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict").build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jtwToken));

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
            Map<String, Object> err = new HashMap<>();
            err.put("error", true);
            err.put("message", "Authentication failed.");
            return ResponseEntity.status(UNAUTHORIZED).body(err);
        }
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication?.name") String email){
        return ResponseEntity.ok(!(email == null));
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOTP(@RequestParam String email){
        try {
            profileService.sendResetOTP(email);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public void sendResetPassword(
            @Valid @RequestBody ResetPasswordRequest request){
        try{
            profileService.resetPassword(request.getEmail(),
                    request.getOtp(), request.getNewPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @PostMapping("/send-otp")
    public void sendVerifyOTP(@CurrentSecurityContext(expression = "authentication?.name") String email){

        try {
            profileService.sendOTP(email);
        } catch (Exception e){
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request,
                            @CurrentSecurityContext(expression = "authentication.name") String email){


        try{
            profileService.verifyOTP(email, request.get("otp").toString());
        } catch (Exception e){
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        ResponseCookie cookie= ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
    }

}
