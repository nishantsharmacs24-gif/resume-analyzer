package com.resumeanalyzer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

// This class handles creating and validating JWT tokens
// JWT = JSON Web Token — used to verify logged-in users

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Create a signing key from our secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // GENERATE TOKEN: Called when user logs in
    // Returns a token string like: eyJhbGciOiJIUzI1NiJ9....
    public String generateToken(String email, String role, Long userId) {
        return Jwts.builder()
                .setSubject(email)              // Who the token is for
                .claim("role", role)            // Extra data: user role
                .claim("userId", userId)        // Extra data: user ID
                .setIssuedAt(new Date())        // When token was created
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Expiry (24hrs)
                .signWith(getSigningKey())      // Sign with our secret key
                .compact();
    }

    // VALIDATE TOKEN: Called on every API request to verify the token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true; // Token is valid
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token is invalid/expired
        }
    }

    // EXTRACT EMAIL from token
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // EXTRACT ROLE from token
    public String getRoleFromToken(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }
}
