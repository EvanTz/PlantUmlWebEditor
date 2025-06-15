package com.example.plantumlwebeditorv2.security.jwt;

import com.example.plantumlwebeditorv2.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Handles all JWT-related operations for the application
 * Compatible with Java 11
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // JWT secret key. Should be set in application.properties
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // Token expiration time in milliseconds. Should be set in application.properties
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;


    // Generate JWT token for authenticated user
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())  // Changed from .subject() to .setSubject()
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))  // Changed from .expiration() to .setExpiration()
                .signWith(key(), SignatureAlgorithm.HS256)  // Added SignatureAlgorithm
                .compact();
    }


    // Create signing key
    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


     // Extract username from JWT token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()  // Changed from parser() to parserBuilder()
                .setSigningKey(key())  // Changed from verifyWith() to setSigningKey()
                .build()
                .parseClaimsJws(token)  // Changed from parseSignedClaims() to parseClaimsJws()
                .getBody()  // Changed from getPayload() to getBody()
                .getSubject();
    }


    // Returns true if token is valid - logging possible errors
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()  // Changed from parser() to parserBuilder()
                    .setSigningKey(key())  // Changed from verifyWith() to setSigningKey()
                    .build()
                    .parseClaimsJws(authToken);  // Changed from parseSignedClaims() to parseClaimsJws()
            return true;

        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}