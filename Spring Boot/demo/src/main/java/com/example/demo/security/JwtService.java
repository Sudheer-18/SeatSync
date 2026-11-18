package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.example.demo.model.Role;
import com.example.demo.model.UserEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String JWT_ALGORITHM = "HS256";
    private static final String MAC_ALGORITHM = "HmacSHA256";
    private final ObjectMapper mapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
        ObjectMapper mapper,
        @Value("${seatsync.jwt.secret}") String secret,
        @Value("${seatsync.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes.");
        }
        if (expirationSeconds < 1) throw new IllegalArgumentException("JWT expiration must be positive.");
        this.mapper = mapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public TokenResult issue(UserEntity user) {
        try {
            String header = encode(Map.of("alg", JWT_ALGORITHM, "typ", "JWT"));
            long expiresAt = Instant.now().getEpochSecond() + expirationSeconds;
            String payload = encode(Map.of(
                "sub", user.getId().toString(), "email", user.getEmail(), "role", user.getRole().name(), "exp", expiresAt));
            String content = header + "." + payload;
            return new TokenResult(content + "." + sign(content), "Bearer", expirationSeconds);
        } catch (GeneralSecurityException | JacksonException exception) {
            throw new IllegalStateException("Unable to create authentication token.", exception);
        }
    }

    public AuthenticatedUser verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Invalid token.");
            JsonNode header = mapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
            if (!JWT_ALGORITHM.equals(header.path("alg").asText()) || !"JWT".equals(header.path("typ").asText())) {
                throw new IllegalArgumentException("Invalid token.");
            }
            String content = parts[0] + "." + parts[1];
            byte[] expected = sign(content).getBytes(StandardCharsets.US_ASCII);
            byte[] actual = parts[2].getBytes(StandardCharsets.US_ASCII);
            if (!MessageDigest.isEqual(expected, actual)) throw new IllegalArgumentException("Invalid token.");
            JsonNode claims = mapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
            if (claims.path("exp").asLong() <= Instant.now().getEpochSecond()) throw new IllegalArgumentException("Expired token.");
            return new AuthenticatedUser(claims.path("sub").asLong(), claims.path("email").asText(), Role.valueOf(claims.path("role").asText()));
        } catch (GeneralSecurityException | JacksonException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid or expired token.", exception);
        }
    }

    private String encode(Object value) throws JacksonException {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, MAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.US_ASCII)));
    }

    public record TokenResult(String token, String tokenType, long expiresIn) { }
}
