package com.project.controller;

import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // ==========================================
    // REGISTER NEW USER
    // POST /api/auth/register
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> request) {
        try {

            // Extract fields from request body
            String fullName = request.get("fullName");
            String email = request.get("email");
            String password = request.get("password");
            String institutionName = request.get("institutionName");
            String roleStr = request.get("role");

            // Validate required fields
            if (fullName == null || fullName.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Full name is required"));
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Email is required"));
            }
            if (password == null || password.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Password is required"));
            }
            if (password.length() < 6) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Password must be at least 6 characters"));
            }

            // Default role to STUDENT if not provided
            User.Role role = User.Role.STUDENT;
            if (roleStr != null && !roleStr.isBlank()) {
                try {
                    role = User.Role.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity
                            .badRequest()
                            .body(errorResponse(
                                    "Invalid role. Use STUDENT or ADMIN"));
                }
            }

            // Register user
            User registeredUser = userService.registerUser(
                    fullName,
                    email,
                    password,
                    institutionName,
                    role
            );

            // Generate JWT token
            String token = jwtUtil.generateToken(registeredUser);

            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("token", token);
            response.put("expiresIn",
                    jwtUtil.getExpiration());
            response.put("user",
                    buildUserResponse(registeredUser));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Registration failed. Please try again"));
        }
    }

    // ==========================================
    // LOGIN USER
    // POST /api/auth/login
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request) {
        try {

            // Extract credentials
            String email = request.get("email");
            String password = request.get("password");

            // Validate required fields
            if (email == null || email.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Email is required"));
            }
            if (password == null || password.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Password is required"));
            }

            // Validate login credentials
            User user = userService.validateLogin(email, password);

            // Log the login event
            userService.logUserLogin(user);

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            // Build success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("expiresIn",
                    jwtUtil.getExpiration());
            response.put("user",
                    buildUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Login failed. Please try again"));
        }
    }

    // ==========================================
    // VALIDATE TOKEN
    // GET /api/auth/validate
    // ==========================================
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Extract token
            if (authHeader == null
                    || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("Invalid token format"));
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtUtil.isValidToken(token)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("Token is invalid or expired"));
            }

            // Extract user info from token
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            String fullName = jwtUtil.extractFullName(token);
            Long userId = jwtUtil.extractUserId(token);
            String institutionName =
                    jwtUtil.extractInstitutionName(token);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", true);
            response.put("email", email);
            response.put("role", role);
            response.put("fullName", fullName);
            response.put("userId", userId);
            response.put("institutionName", institutionName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Token validation failed"));
        }
    }

    // ==========================================
    // GET CURRENT USER PROFILE
    // GET /api/auth/me
    // ==========================================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        try {

            // Extract token
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            // Load user from database
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(
                            "User not found"));

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", buildUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Failed to get user profile"));
        }
    }

    // ==========================================
    // CHANGE PASSWORD
    // PUT /api/auth/change-password
    // ==========================================
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {

            // Extract user from token
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            // Validate fields
            if (currentPassword == null
                    || currentPassword.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "Current password is required"));
            }
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "New password is required"));
            }
            if (newPassword.length() < 6) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse(
                                "New password must be at least"
                                        + " 6 characters"));
            }

            // Change password
            userService.changePassword(
                    userId,
                    currentPassword,
                    newPassword);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Password changed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Password change failed"));
        }
    }

    // ==========================================
    // UPDATE PROFILE
    // PUT /api/auth/update-profile
    // ==========================================
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {

            // Extract user from token
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            String fullName = request.get("fullName");
            String institutionName = request.get("institutionName");

            // Validate fields
            if (fullName == null || fullName.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(errorResponse("Full name is required"));
            }

            // Update profile
            User updatedUser = userService.updateProfile(
                    userId,
                    fullName,
                    institutionName);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Profile updated successfully");
            response.put("user",
                    buildUserResponse(updatedUser));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse(
                            "Profile update failed"));
        }
    }

    // ==========================================
    // DELETE / DEACTIVATE OWN ACCOUNT
    // DELETE /api/auth/delete-account
    // ==========================================
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteOwnAccount(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            userService.deactivateOwnAccount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to delete account"));
        }
    }

    // ==========================================
    // HELPER - BUILD USER RESPONSE MAP
    // ==========================================
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("fullName", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().name());
        userMap.put("institutionName", user.getInstitutionName());
        userMap.put("isActive", user.isActive());
        userMap.put("createdAt", user.getCreatedAt());
        return userMap;
    }

    // ==========================================
    // HELPER - BUILD ERROR RESPONSE MAP
    // ==========================================
    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}
