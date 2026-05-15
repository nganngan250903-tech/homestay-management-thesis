package com.example.homestaymanager.service;

import com.example.homestaymanager.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMs;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:homestay-manager-development-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(int userId, String email, String userType, String role) {
        long now = Instant.now().getEpochSecond();
        long exp = now + expirationMs / 1000;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("id", userId);
        payload.put("email", email);
        payload.put("userType", userType);
        payload.put("role", role);
        payload.put("iat", now);
        payload.put("exp", exp);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encodeJson(Map<String, Object> data) {
        try {
            return base64Url(objectMapper.writeValueAsBytes(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot create JWT payload", e);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(key);
            return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Cannot sign JWT", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // Verify signature
            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!parts[2].equals(expectedSignature)) {
                return null; // Signature verification failed
            }
            
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

            long exp = ((Number) payload.get("exp")).longValue();
            long now = Instant.now().getEpochSecond();
            if (exp < now) {
                return null;
            }

            int id = ((Number) payload.get("id")).intValue();
            String email = (String) payload.get("email");
            String userType = (String) payload.get("userType");
            String role = (String) payload.get("role");

            return AuthenticatedUser.builder()
                    .id(id)
                    .email(email)
                    .userType(userType)
                    .role(role)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
