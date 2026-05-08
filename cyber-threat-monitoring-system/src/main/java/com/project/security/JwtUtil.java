package com.project.security;

import com.project.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // ==========================================
    // GET SIGNING KEY
    // ==========================================
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==========================================
    // GENERATE TOKEN FOR USER
    // ==========================================
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        // Add custom claims
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("institutionName", user.getInstitutionName());
        claims.put("userId", user.getId());

        return createToken(claims, user.getEmail());
    }

    // ==========================================
    // CREATE TOKEN
    // ==========================================
    private String createToken(Map<String, Object> claims,
                               String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ==========================================
    // EXTRACT ALL CLAIMS FROM TOKEN
    // ==========================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==========================================
    // EXTRACT SPECIFIC CLAIM
    // ==========================================
    public <T> T extractClaim(String token,
                              Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ==========================================
    // EXTRACT EMAIL (SUBJECT) FROM TOKEN
    // ==========================================
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ==========================================
    // EXTRACT EXPIRATION DATE FROM TOKEN
    // ==========================================
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ==========================================
    // EXTRACT ROLE FROM TOKEN
    // ==========================================
    public String extractRole(String token) {
        return extractClaim(token,
                claims -> claims.get("role", String.class));
    }

    // ==========================================
    // EXTRACT FULL NAME FROM TOKEN
    // ==========================================
    public String extractFullName(String token) {
        return extractClaim(token,
                claims -> claims.get("fullName", String.class));
    }

    // ==========================================
    // EXTRACT USER ID FROM TOKEN
    // ==========================================
    public Long extractUserId(String token) {
        return extractClaim(token,
                claims -> claims.get("userId", Long.class));
    }

    // ==========================================
    // EXTRACT INSTITUTION FROM TOKEN
    // ==========================================
    public String extractInstitutionName(String token) {
        return extractClaim(token,
                claims -> claims.get("institutionName", String.class));
    }

    // ==========================================
    // CHECK IF TOKEN IS EXPIRED
    // ==========================================
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ==========================================
    // VALIDATE TOKEN AGAINST USER
    // ==========================================
    public boolean validateToken(String token, String userEmail) {
        try {
            final String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(userEmail)
                    && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // VALIDATE TOKEN STRUCTURE ONLY
    // ==========================================
    public boolean isValidToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // GET TOKEN EXPIRATION TIME IN MS
    // ==========================================
    public Long getExpiration() {
        return expiration;
    }
}
