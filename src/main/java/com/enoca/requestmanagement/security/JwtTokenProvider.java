package com.enoca.requestmanagement.security;

import com.enoca.requestmanagement.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final int MINIMUM_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(properties.secret()));
        this.expirationMs = properties.expirationMs();
    }

    private static byte[] decodeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT imza anahtari tanimli degil. JWT_SECRET ortam degiskenini ayarlayin. "
                            + "Yeni bir anahtar uretmek icin: openssl rand -base64 48");
        }

        byte[] key;
        try {
            key = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "JWT imza anahtari gecerli bir Base64 degeri degil. "
                            + "Yeni bir anahtar uretmek icin: openssl rand -base64 48", ex);
        }

        if (key.length < MINIMUM_KEY_BYTES) {
            throw new IllegalStateException(
                    "JWT imza anahtari cok kisa: %d bayt. HS256 icin en az %d bayt gerekir. "
                            .formatted(key.length, MINIMUM_KEY_BYTES)
                            + "Yeni bir anahtar uretmek icin: openssl rand -base64 48");
        }

        return key;
    }

    public String generateToken(String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Geçersiz JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
