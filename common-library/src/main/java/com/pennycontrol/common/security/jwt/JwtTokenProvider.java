package com.pennycontrol.common.security.jwt;

import com.pennycontrol.common.dto.UserPrincipal;
import com.pennycontrol.common.security.jwt.JwtProperties;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(UserPrincipal userPrincipal) {
        return generateToken(userPrincipal, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        return generateToken(userPrincipal, jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Generate token with custom expiration
     */
    private String generateToken(UserPrincipal userPrincipal, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("roles", userPrincipal.getRoles());

        return Jwts.builder()
                .subject(userPrincipal.getEmail()) // Use email as subject
                .claims(claims)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extract email from token (subject contains email in our system)
     */
    public String getEmailFromTokenSubject(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extract user ID from token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Set.copyOf(claims.get("roles", java.util.List.class));
    }

    /**
     * Extract UserPrincipal from token
     */
    public UserPrincipal getUserPrincipalFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        @SuppressWarnings("unchecked")
        Set<String> roles = Set.copyOf(claims.get("roles", java.util.List.class));

        return UserPrincipal.builder()
                .id(userId)
                .email(email)
                .roles(roles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "JWT claims string is empty");
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Parse claims from token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
