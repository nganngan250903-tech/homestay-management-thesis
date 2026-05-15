package com.example.homestaymanager.service;

import com.example.homestaymanager.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTests {

    @Autowired
    private JwtService jwtService;

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
        assertTrue(token.contains("."), "Token should be in JWT format (contains dots)");

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
        // Act: Try to parse invalid token
        AuthenticatedUser result = jwtService.parseToken("invalid.token.format");
        
        // Assert: Should return null for invalid token
        assertNull(result, "Invalid token should return null");
    }

    @Test
    void testParseExpiredToken() throws Exception {
        // For expired token test, we would need to mock the time or test with an already-expired token
        // This is a placeholder test showing the concept
        AuthenticatedUser result = jwtService.parseToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjEyMyIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsInVzZXJUeXBlIjoiQ1VTVE9NRVIiLCJyb2xlIjoiQ1VTVE9NRVIiLCJleHAiOjE2MDAwMDAwMDB9.invalid");
        
        // Should return null for invalid signature
        assertNull(result, "Invalid signature should return null");
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
}
