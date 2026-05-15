# Homestay Manager API - Security Implementation Guide

## Overview

The Homestay Manager backend has been enhanced with comprehensive role-based access control (RBAC) and JWT authentication. This guide explains the security architecture, role permissions, and how to use the API.

## Security Architecture

### JWT Authentication Flow

```
Client Request (with JWT in Authorization header)
        ↓
JwtAuthenticationFilter
        ↓
Extract "Bearer <token>" from header
        ↓
JwtService.parseToken(token)
        ↓
Validate signature, expiration, structure
        ↓
Create AuthenticatedUser and store in SecurityContext
        ↓
Controller receives request with authenticated user
        ↓
SecurityUtil.getCurrentUser() retrieves user info
        ↓
Role-based authorization checks in controller
```

### JWT Token Structure

Tokens are generated in standard JWT format (header.payload.signature):

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "123",
  "id": 123,
  "email": "user@example.com",
  "userType": "CUSTOMER",
  "role": "CUSTOMER",
  "iat": 1715856000,
  "exp": 1715942400
}
```

**Signature:** HMAC-SHA256(header.payload, secret)

### Token Validation

The `JwtService.parseToken()` method performs these validations:

1. **Format Validation**: Token must have exactly 3 parts (header.payload.signature)
2. **Signature Verification**: Recalculates HMAC-SHA256 and compares with provided signature
3. **Expiration Check**: Validates `exp` claim is greater than current Unix timestamp
4. **Payload Decoding**: Base64 decodes payload and extracts claims

Invalid tokens return `null` and request is rejected.

## Role-Based Access Control

### Three User Roles

#### 1. CUSTOMER (Guest/Customer)
- **User Type**: CUSTOMER
- **Permissions**:
  - ✅ View public room information (GET /rooms, /roomTypes, /branches, etc.)
  - ✅ View own profile (GET /customers/{id} where id = current user)
  - ✅ Update own profile (PATCH /customers/{id})
  - ✅ Create bookings for themselves (POST /bookings)
  - ✅ View own bookings (GET /bookings, filtered to own only)
  - ✅ Cancel own bookings (POST /bookings/{id}/cancel)
  - ❌ Cannot view other customers' information
  - ❌ Cannot update status of bookings
  - ❌ Cannot access employee/admin functions

#### 2. EMPLOYEE (Staff)
- **User Type**: EMPLOYEE
- **Role**: Various (RECEPTIONIST, MANAGER, etc.)
- **Permissions**:
  - ✅ View all customers
  - ✅ View all rooms and room information
  - ✅ View all bookings
  - ✅ Update booking status (PENDING → CONFIRMED, CANCELLED, etc.)
  - ✅ Cancel bookings
  - ✅ View own employee profile
  - ✅ Update own employee profile
  - ❌ Cannot create new customers/employees
  - ❌ Cannot manage system configuration

#### 3. ADMIN (Administrator)
- **User Type**: ADMIN
- **Role**: ADMIN
- **Permissions**:
  - ✅ Full access to all operations
  - ✅ Create/Read/Update/Delete all entities (rooms, bookings, customers, employees, etc.)
  - ✅ Manage system configuration
  - ✅ View all reports and data

### Authorization by Endpoint

#### Customer Endpoints

| Endpoint | Method | CUSTOMER | EMPLOYEE | ADMIN | Notes |
|----------|--------|----------|----------|-------|-------|
| /customers | GET | ❌ | ❌ | ✅ | Admin only |
| /customers | POST | ❌ | ❌ | ✅ | Admin only |
| /customers/{id} | GET | ✅ own | ✅ all | ✅ all | Customer: own only |
| /customers/{id} | PATCH | ✅ own | ❌ | ✅ all | Customer: own only |
| /customers/{id} | DELETE | ❌ | ❌ | ✅ | Admin only |

#### Booking Endpoints

| Endpoint | Method | CUSTOMER | EMPLOYEE | ADMIN | Notes |
|----------|--------|----------|----------|-------|-------|
| /bookings | GET | ✅ own | ✅ all | ✅ all | Customer: filtered to own bookings |
| /bookings | POST | ✅ | ❌ | ❌ | Customer only; must create for themselves |
| /bookings/{id} | GET | ✅ own | ✅ all | ✅ all | Customer: own only |
| /bookings/{id}/status | PATCH | ❌ | ✅ | ✅ | Employee/Admin only |
| /bookings/{id}/cancel | POST | ✅ own | ✅ any | ✅ any | Customer: own only |

#### Employee Endpoints

| Endpoint | Method | CUSTOMER | EMPLOYEE | ADMIN | Notes |
|----------|--------|----------|----------|-------|-------|
| /employees | GET | ❌ | ❌ | ✅ | Admin only |
| /employees | POST | ❌ | ❌ | ✅ | Admin only |
| /employees/{id} | GET | ❌ | ✅ own | ✅ all | Employee: own only |
| /employees/{id} | PATCH | ❌ | ✅ own | ✅ all | Employee: own only |
| /employees/{id} | DELETE | ❌ | ❌ | ✅ | Admin only |

#### Public Endpoints (No Authentication Required)

```
GET /rooms
GET /rooms/{id}
GET /roomTypes
GET /roomTypes/{id}
GET /branches
GET /branches/{id}
GET /amenities
GET /categories
GET /roomPricings
GET /roomPhotos
POST /auth/login
GET /auth/verify
```

## API Usage

### 1. Generate Authentication Token

To interact with protected endpoints, first obtain a JWT token.

**Request:**
```
POST /auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "status": "OK",
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJpZCI6MTIzLCJlbWFpbCI6ImN1c3RvbWVyQGV4YW1wbGUuY29tIiwidXNlclR5cGUiOiJDVVNUT01FUiIsInJvbGUiOiJDVVNUT01FUiIsImlhdCI6MTcxNTg1NjAwMCwiZXhwIjoxNzE1OTQyNDAwfQ.signature",
    "userId": 123,
    "userType": "CUSTOMER",
    "email": "customer@example.com"
  }
}
```

### 2. Make Authenticated Requests

Include the token in the Authorization header:

```
GET /bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Error Handling

**Unauthorized (No Token):**
```json
{
  "status": "UNAUTHORIZED",
  "message": "Authorization header missing or invalid",
  "data": null
}
```

**Forbidden (Insufficient Permissions):**
```json
{
  "status": "FORBIDDEN",
  "message": "Không có quyền xem thông tin khách hàng này",
  "data": null
}
```

**Invalid Token:**
```json
{
  "status": "UNAUTHORIZED",
  "message": "Invalid or expired token",
  "data": null
}
```

## Implementation Details

### Key Classes

1. **JwtService** (`service/JwtService.java`)
   - `generateToken(userId, email, userType, role)`: Creates JWT
   - `parseToken(token)`: Validates and decodes JWT

2. **AuthenticatedUser** (`security/AuthenticatedUser.java`)
   - `isAdmin()`: Returns true if role is "ADMIN"
   - `isEmployee()`: Returns true if role is "EMPLOYEE" or is admin
   - `isCustomer()`: Returns true if role is "CUSTOMER"

3. **SecurityUtil** (`security/SecurityUtil.java`)
   - `getCurrentUser()`: Gets authenticated user from SecurityContext
   - `isAdmin()`, `isEmployee()`, `isCustomer()`: Helper methods

4. **JwtAuthenticationFilter** (`security/JwtAuthenticationFilter.java`)
   - Intercepts HTTP requests
   - Extracts JWT from Authorization header
   - Validates token and sets authentication

5. **SecurityConfig** (`security/SecurityConfig.java`)
   - Configures Spring Security
   - Sets permit-all endpoints
   - Enables JWT filter
   - Configures CORS (if needed)

### Booking Timeout Implementation

Bookings automatically transition from PENDING to EXPIRED after 10 minutes:

- When booking created: `pendingExpiresAt = now + 10 minutes`
- Scheduled task runs every 60 seconds to check and expire old bookings
- `expirePendingBookings()` runs via `@Scheduled(fixedDelay = 60000)`
- Affected bookings: `status = PENDING AND pendingExpiresAt < now`

### Refund Calculation

Booking cancellations compute refund percentages:
- **> 24 hours before checkin**: 100% refund
- **< 24 hours before checkin**: 50% refund
- **After checkin**: 0% refund

## Security Configuration

### JWT Settings (application.yaml)

```yaml
app:
  jwt:
    secret: "your-256-bit-secret-key-change-in-production"
    expiration-ms: 86400000  # 24 hours
```

### CORS Configuration

If frontend is on different domain, CORS is configured in `SecurityConfig`:

```java
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourdomain.com"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## Testing

Run JWT service unit tests:

```bash
cd backend/homestay-management-thesis
.\mvnw.cmd test -Dtest=JwtServiceUnitTest
```

Expected output:
```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Deployment Checklist

- [ ] Change JWT secret in `application.yaml` (use strong, unique key)
- [ ] Set database environment variables (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- [ ] Configure CORS origins for production domain
- [ ] Set JWT expiration time appropriate for your security needs
- [ ] Enable HTTPS for all endpoints
- [ ] Review and test all role-based access control rules
- [ ] Set up monitoring for unauthorized access attempts
- [ ] Configure rate limiting to prevent token brute-forcing
- [ ] Set up secure logging (don't log tokens or sensitive data)

## Troubleshooting

### "Authorization header missing or invalid"
- Ensure token is sent in format: `Authorization: Bearer <token>`
- Check token hasn't expired
- Verify token is valid JWT format

### "Không có quyền..." (Permission denied)
- Verify user role matches endpoint requirements
- Check if trying to access another user's data as non-admin
- Some endpoints only allow specific roles

### Token signature verification failed
- JWT secret in production must match configuration
- Don't use development secret in production
- If secret changes, existing tokens become invalid

## Future Enhancements

1. **Refresh Tokens**: Implement token refresh mechanism
2. **Role Management**: Dynamic role creation and permission assignment
3. **OAuth2/OIDC**: Support third-party authentication providers
4. **Two-Factor Authentication**: Add 2FA for enhanced security
5. **Audit Logging**: Log all authentication and authorization events
6. **API Keys**: Support API key authentication for service-to-service calls
