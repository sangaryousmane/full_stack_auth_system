package com.ous.aethererp.jwtUtils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Marks this class as a Spring-managed component, so it can be injected where needed
@Component
public class JWTUtils {

    // Injects the secret key value from application.properties (jwt.secret.key)
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; //15 minutes

    // Generates a JWT token for a given user
    public String generateToken(UserDetails userDetails){
        // Create an empty claims map (can hold custom data if needed)
        Map<String, Object> claims = new HashMap<>();
        // Call helper method to build the token with claims and username
        return createClaims(claims, userDetails.getUsername());
    }

    // Builds the JWT with claims, subject (email/username), issue date, expiration, and signature
    private String createClaims(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims) // Add custom claims
                .setSubject(email) // Set subject (usually username or email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token creation time
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                // Expiration set to 10 hours from now
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Sign with HS256 algorithm and secret key
                .compact(); // Build and return the token as a string
    }

    // Extracts all claims (payload) from a given JWT token
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // Use the same secret key to validate signature
                .parseClaimsJws(token) // Parse the token
                .getBody(); // Return the claims (payload)
    }

    // Extracts a specific claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims); // Apply resolver to extract desired claim
    }

    // Extracts the subject (email/username) from the token
    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Checks if the token is expired
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Validates the token by checking:
    // 1. The subject (email/username) matches the user
    // 2. The token is not expired
    public Boolean validateToken(String token, UserDetails userDetails){
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
