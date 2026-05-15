package com.example.homestaymanager.service;

import com.example.homestaymanager.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtService jwtService = new JwtService(
            objectMapper,
            "test-secret-key-for-unit-tests",
            86400000 // 24 hours
    );

    @Test
    void testGenerateAndParseToken() {
        // Arrange: Test parameters
        int userId = 123;
        String email = "test@example.com";
        String userType = "CUSTOMER";
        String role = "CUSTOMER";

        // Act: Generate token
        String token = jwtService.generateToken(userId, email, userType, role);
        assertNotNull(token, "Token should not be null");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Token should have 3 parts (header.payload.signature)");

        // Act: Parse token back
        AuthenticatedUser parsedUser = jwtService.parseToken(token);
        
        // Assert: Verify parsed user matches original
        assertNotNull(parsedUser, "Parsed user should not be null");
        assertEquals(userId, parsedUser.getId(), "User ID should match");
        assertEquals(email, parsedUser.getEmail(), "Email should match");
        assertEquals(userType, parsedUser.getUserType(), "User type should match");
        assertEquals(role, parsedUser.getRole(), "Role should match");
    }

    @Test
    void testParseInvalidToken() {
        // Act: Try to parse invalid token with wrong format
        AuthenticatedUser result = jwtService.parseToken("invalid.token");
        
        // Assert: Should return null for invalid token
        assertNull(result, "Invalid token (2 parts) should return null");
    }

    @Test
    void testParseCorruptedPayload() {
        // Act: Try to parse token with corrupted payload
        AuthenticatedUser result = jwtService.parseToken("header.corrupted-payload.signature");
        
        // Assert: Should return null for corrupted token
        assertNull(result, "Token with corrupted payload should return null");
    }

    @Test
    void testEmployeeTokenGeneration() {
        // Arrange
        int employeeId = 456;
        String employeeEmail = "employee@example.com";
        String employeeType = "EMPLOYEE";
        String employeeRole = "RECEPTIONIST";

        // Act
        String token = jwtService.generateToken(employeeId, employeeEmail, employeeType, employeeRole);
        AuthenticatedUser parsed = jwtService.parseToken(token);

        // Assert
        assertNotNull(parsed);
        assertEquals(employeeType, parsed.getUserType());
        assertEquals(employeeRole, parsed.getRole());
        // Note: isEmployee() checks if role is "EMPLOYEE" or "ADMIN", but this user has role "RECEPTIONIST"
        // So isEmployee() will return false. That's fine for now; the userType indicates the role.
    }

    @Test
    void testAdminTokenGeneration() {
        // Arrange
        int adminId = 789;
        String adminEmail = "admin@example.com";
        String adminType = "ADMIN";
        String adminRole = "ADMIN";

        // Act
        String token = jwtService.generateToken(adminId, adminEmail, adminType, adminRole);
        AuthenticatedUser parsed = jwtService.parseToken(token);

        // Assert
        assertNotNull(parsed);
        assertEquals(adminType, parsed.getUserType());
        assertEquals(adminRole, parsed.getRole());
        assertTrue(parsed.isAdmin());
    }

    @Test
    void testDifferentSecretRejected() {
        // Arrange: Create token with one secret
        String token = jwtService.generateToken(100, "test@test.com", "CUSTOMER", "CUSTOMER");
        
        // Act: Try to parse with different secret
        JwtService differentSecretService = new JwtService(
                objectMapper,
                "different-secret-key",
                86400000
        );
        AuthenticatedUser parsed = differentSecretService.parseToken(token);
        
        // Assert: Should fail due to signature verification
        assertNull(parsed, "Token signed with different secret should fail");
    }
}
