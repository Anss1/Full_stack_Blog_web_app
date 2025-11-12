package com.anas.springblog.utility;

import com.anas.springblog.authentecation.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService implements IJwtUtil {
    public static final String CLAIM_KEY_ID = "id";
    public static final String CLAIM_KEY_EMAIL = "email";
    public static final String CLAIM_KEY_ROLE = "role";

    @Value("${app.security.jwt.access-token-expiration-ms}")
    private long EXPIRATION_TIME ; // 15 minutes

    private final SecretKey SECRET_KEY;

    public JwtService(@Value("${jwt.secret.base64}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {
        return buildToken(user.getId(), user.getUsername(), user.getRole().name(), user.getEmail());
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String username, String token) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Long id, String username, String role, String email) {
        // making claims map
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_ID, id);
        claims.put(CLAIM_KEY_EMAIL, email);
        claims.put(CLAIM_KEY_ROLE, role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSignKey() {
        return this.SECRET_KEY;
    }

}
