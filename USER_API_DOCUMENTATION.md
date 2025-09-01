# Library Management System - User Management API Documentation

## Table of Contents
- [Base URL and Authentication](#base-url-and-authentication)
- [Endpoints](#endpoints)
  - [1. User Registration](#1-user-registration)
  - [2. Email Verification](#2-email-verification)
  - [3. User Login](#3-user-login)
  - [4. User Logout](#4-user-logout)
  - [5. Get Current User Profile](#5-get-current-user-profile)
  - [6. Update User Profile](#6-update-user-profile)
  - [7. Password Reset Request](#7-password-reset-request)
  - [8. Reset Password](#8-reset-password)
- [Admin Endpoints](#admin-endpoints)
  - [1. Get User by Username](#1-get-user-by-username-admin)
  - [2. Delete User](#2-delete-user-admin)
  - [3. Suspend User](#3-suspend-user-admin)
  - [4. Activate User](#4-activate-user-admin)
- [Error Handling](#error-handling)
- [Frontend Implementation Notes](#frontend-implementation-notes)

## Base URL and Authentication
- **Base URL:** `/user`
- **Authentication:** Most endpoints require a JWT token in the Authorization header
- **Token Format:** `Bearer <your_jwt_token>`

## Endpoints

### 1. User Registration
**Endpoint:** `POST /user/register`  
**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```
**Response:**
```json
{
  "message": "User registered successfully"
}
```
**Notes:**
- Sends a verification email to the provided email address
- Password must be at least 8 characters long

### 2. Email Verification
**Endpoint:** `GET /user/verify-email?token={verification_token}`  
**Response (Success):**
```json
{
  "message": "Email verified successfully"
}
```
**Notes:**
- The verification token is sent to the user's email after registration

### 3. User Login
**Endpoint:** `POST /user/login`  
**Request Body:**
```json
{
  "emailOrUsername": "string",
  "password": "string"
}
```
**Response:**
```json
{
  "token": "jwt_token_here"
}
```

### 4. User Logout
**Endpoint:** `POST /user/logout`  
**Headers:**
```
Authorization: Bearer <token>
```
**Response:**
```json
{
  "message": "Logged out successfully"
}
```

### 5. Get Current User Profile
**Endpoint:** `GET /user/profile`  
**Headers:**
```
Authorization: Bearer <token>
```
**Response:**
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "role": "USER",
  "firstName": "string",
  "lastName": "string",
  "country": "string",
  "address": "string",
  "profileImageUrl": "string",
  "bio": "string",
  "phoneNumber": "string"
}
```

### 6. Update User Profile
**Endpoint:** `PATCH /user/profile`  
**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "country": "string",
  "address": "string",
  "bio": "string",
  "phoneNumber": "string"
}
```
**Response:** Updated user profile (same structure as GET /user/profile)

### 7. Password Reset Request
**Endpoint:** `POST /user/redeem-password`  
**Request Body:**
```json
{
  "email": "user@example.com"
}
```
**Response:**
```json
{
  "message": "Password reset link sent to your email"
}
```

### 8. Reset Password
**Endpoint:** `POST /user/reset-password`  
**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "password": "new_password"
}
```
**Response:**
```json
{
  "message": "Password reset successful"
}
```

## Admin Endpoints

### 1. Get User by Username (Admin)
**Endpoint:** `GET /user/admin/users/{username}`  
**Headers:**
```
Authorization: Bearer <admin_token>
```
**Response:** User profile (same structure as GET /user/profile)

### 2. Delete User (Admin)
**Endpoint:** `DELETE /user/admin/users/{username}`  
**Headers:**
```
Authorization: Bearer <admin_token>
```
**Response:**
```json
{
  "message": "User deleted successfully"
}
```

### 3. Suspend User (Admin)
**Endpoint:** `PATCH /user/admin/users/{username}/suspend`  
**Headers:**
```
Authorization: Bearer <admin_token>
```
**Response:**
```json
{
  "message": "User suspended successfully"
}
```

### 4. Activate User (Admin)
**Endpoint:** `PATCH /user/admin/users/{username}/activate`  
**Headers:**
```
Authorization: Bearer <admin_token>
```
**Response:**
```json
{
  "message": "User activated successfully"
}
```

## Error Handling
Common error responses include:

```json
{
  "error": "Error message here"
}
```

**Status Codes:**
- 200: Success
- 400: Bad Request (validation errors, etc.)
- 401: Unauthorized (missing or invalid token)
- 403: Forbidden (insufficient permissions)
- 404: Not Found (resource not found)

## Frontend Implementation Notes
1. **Token Storage**:
   - Store the JWT token in localStorage or httpOnly cookies after successful login
   - Implement token refresh logic if your application supports it

2. **Request Headers**:
   - Include the token in the Authorization header for authenticated requests:
     ```
     Authorization: Bearer <your_jwt_token>
     ```
   - Set Content-Type header for requests with body:
     ```
     Content-Type: application/json
     ```

3. **Error Handling**:
   - Implement global error handling for:
     - 401 Unauthorized: Redirect to log in
     - 403 Forbidden: Show access denied message
     - 400 Bad Request: Display validation errors to the user
     - 500 Internal Server Error: Show generic error message

4. **Form Validation**:
   - Implement client-side validation to match backend validation rules
   - Show clear error messages for invalid inputs

5. **Loading States**:
   - Show loading indicators during API calls
   - Disable submit buttons while requests are in progress

6. **Security**:
   - Never store sensitive information in localStorage
   - Implement proper CORS policies
   - Use HTTPS in production

7. **User Experience**:
   - Provide feedback for all user actions (success/error messages)
   - Implement proper redirects after actions (e.g., after login, redirect to dashboard)
   - Handle token expiration gracefully

8. **Testing**:
   - Test all API endpoints with various scenarios
   - Test error cases and edge cases
   - Verify proper handling of expired tokens

This documentation provides a complete reference for frontend developers to integrate with the User Management API. The endpoints support all CRUD operations for user management, including authentication, authorization, and profile management.
