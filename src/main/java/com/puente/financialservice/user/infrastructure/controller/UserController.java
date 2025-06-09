package com.puente.financialservice.user.infrastructure.controller;

import com.puente.financialservice.user.application.dto.UserDTO;
import com.puente.financialservice.user.application.dto.UserRoleUpdateDTO;
import com.puente.financialservice.user.application.dto.UserUpdateDTO;
import com.puente.financialservice.user.application.service.UserService;
import com.puente.financialservice.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users (requires ADMIN role)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/users - Get all users (ADMIN only)");
        
        try {
            List<UserDTO> users = userService.getAllUsers();
            
            logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/users - Retrieved {} users", users.size());
            logger.info("👥 Users in system: {}", 
                users.stream().map(u -> u.getEmail() + "(" + u.getRole() + ")").toList());
            
            return ResponseEntity.ok(users);
            
        } catch (SecurityException e) {
            logger.error("🚫 ENDPOINT SECURITY ERROR: GET /api/v1/users - Access denied: {}", e.getMessage());
            logger.error("🔍 Security details: User might not have ADMIN role");
            throw e;
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: GET /api/v1/users - Failed to retrieve users: {}", e.getMessage());
            logger.error("🔍 Error type: {}", e.getClass().getSimpleName());
            logger.error("📚 Stack trace: ", e);
            throw e;
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the current user's information")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/users/me - Get current user info");
        logger.info("👤 Authenticated user: {} (ID: {})", user.getEmail(), user.getId());
        
        try {
            UserDTO userDTO = userService.getUserById(user.getId());
            
            logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/users/me - User info retrieved");
            logger.info("📄 User details: Name={}, Email={}, Role={}", 
                userDTO.getName(), userDTO.getEmail(), userDTO.getRole());
            
            return ResponseEntity.ok(userDTO);
            
        } catch (SecurityException e) {
            logger.error("🚫 ENDPOINT SECURITY ERROR: GET /api/v1/users/me - Authentication failed: {}", e.getMessage());
            logger.error("🔍 Security details: Invalid or expired token");
            throw e;
        } catch (RuntimeException e) {
            logger.error("❌ ENDPOINT RUNTIME ERROR: GET /api/v1/users/me - User not found or data error: {}", e.getMessage());
            logger.error("🔍 Runtime error type: {}", e.getClass().getSimpleName());
            throw e;
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: GET /api/v1/users/me - Failed to get user info: {}", e.getMessage());
            logger.error("🔍 Error type: {}", e.getClass().getSimpleName());
            logger.error("📚 Stack trace: ", e);
            throw e;
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId, authentication)")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID (requires ADMIN role or own user)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/users/{} - Get user by ID", userId);
        
        try {
            UserDTO user = userService.getUserById(userId);
            
            logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/users/{} - User found", userId);
            logger.info("👤 User details: Name={}, Email={}, Role={}", 
                user.getName(), user.getEmail(), user.getRole());
            
            return ResponseEntity.ok(user);
            
        } catch (SecurityException e) {
            logger.error("🚫 ENDPOINT SECURITY ERROR: GET /api/v1/users/{} - Access denied: {}", userId, e.getMessage());
            logger.error("🔍 Security details: User might not have permission to view user {}", userId);
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                logger.error("👤 ENDPOINT NOT_FOUND ERROR: GET /api/v1/users/{} - User does not exist", userId);
                logger.error("🔍 Database search failed for user ID: {}", userId);
            } else {
                logger.error("❌ ENDPOINT RUNTIME ERROR: GET /api/v1/users/{} - Runtime error: {}", userId, e.getMessage());
                logger.error("🔍 Runtime error type: {}", e.getClass().getSimpleName());
            }
            throw e;
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: GET /api/v1/users/{} - Failed to get user: {}", userId, e.getMessage());
            logger.error("🔍 Error type: {}", e.getClass().getSimpleName());
            logger.error("📚 Stack trace: ", e);
            throw e;
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId, authentication)")
    @Operation(
        summary = "Update user", 
        description = "Updates a user's information. Admin can update any user, while regular users can only update their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Non-admin users can only update their own profile"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO updateDTO,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        logger.info("🌐 ENDPOINT CALLED: PUT /api/v1/users/{} - Update user", userId);
        logger.info("👤 Request by: {} (ID: {}, Role: {})", 
            currentUser.getEmail(), 
            currentUser.getId(), 
            currentUser.getRole());
        logger.info("📝 Update data: Name={}, Email={}, Password={}", 
            updateDTO.getName(), 
            updateDTO.getEmail(),
            updateDTO.getPassword() != null ? "[PROVIDED]" : "[NOT PROVIDED]");
        
        try {
            UserDTO updatedUser = userService.updateUser(userId, updateDTO);
            
            boolean isSelfUpdate = currentUser.getId().equals(userId);
            String updateType = isSelfUpdate ? "self update" : "admin update of other user";
            logger.info("✅ ENDPOINT SUCCESS: PUT /api/v1/users/{} - User updated successfully ({})", userId, updateType);
            logger.info("📄 Updated user: Name={}, Email={}", updatedUser.getName(), updatedUser.getEmail());
            
            return ResponseEntity.ok(updatedUser);
            
        } catch (SecurityException e) {
            logger.error("🚫 ENDPOINT SECURITY ERROR: PUT /api/v1/users/{} - Access denied: {}", userId, e.getMessage());
            logger.error("🔍 Security details: User might not have permission to update user {}", userId);
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email already exists")) {
                logger.error("📧 ENDPOINT VALIDATION ERROR: PUT /api/v1/users/{} - Email conflict: {}", userId, updateDTO.getEmail());
                logger.error("🔍 Attempted to change to existing email: {}", updateDTO.getEmail());
            } else if (e.getMessage().contains("User not found")) {
                logger.error("👤 ENDPOINT NOT_FOUND ERROR: PUT /api/v1/users/{} - User does not exist", userId);
                logger.error("🔍 Database search failed for user ID: {}", userId);
            } else {
                logger.error("❌ ENDPOINT RUNTIME ERROR: PUT /api/v1/users/{} - Runtime error: {}", userId, e.getMessage());
                logger.error("🔍 Runtime error type: {}", e.getClass().getSimpleName());
            }
            throw e;
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: PUT /api/v1/users/{} - Update failed: {}", userId, e.getMessage());
            logger.error("🔍 Error type: {}", e.getClass().getSimpleName());
            logger.error("📚 Stack trace: ", e);
            throw e;
        }
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Updates the role of a user (any authenticated user)")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateDTO roleUpdateDTO,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        logger.info("🌐 ENDPOINT CALLED: PUT /api/v1/users/{}/role - Update user role", userId);
        logger.info("👤 Requested by: {} (ID: {}, Role: {})", currentUser.getEmail(), currentUser.getId(), currentUser.getRole());
        logger.info("🏷️  Role change for user {}: New role = {}", userId, roleUpdateDTO.getRole());
        
        try {
            UserDTO updatedUser = userService.updateUserRole(userId, roleUpdateDTO);
            
            logger.info("✅ ENDPOINT SUCCESS: PUT /api/v1/users/{}/role - Role updated successfully", userId);
            logger.info("🎯 User role changed: {} now has role {}", updatedUser.getEmail(), updatedUser.getRole());
            
            return ResponseEntity.ok(updatedUser);
            
        } catch (SecurityException e) {
            logger.error("🚫 ENDPOINT SECURITY ERROR: PUT /api/v1/users/{}/role - Authentication failed: {}", userId, e.getMessage());
            logger.error("🔍 Security details: User authentication invalid");
            logger.error("👤 Attempted by: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                logger.error("👤 ENDPOINT NOT_FOUND ERROR: PUT /api/v1/users/{}/role - Target user does not exist", userId);
                logger.error("🔍 Database search failed for user ID: {}", userId);
                logger.error("👤 Attempted by: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            } else if (e.getMessage().contains("Invalid role")) {
                logger.error("🏷️  ENDPOINT VALIDATION ERROR: PUT /api/v1/users/{}/role - Invalid role: {}", userId, roleUpdateDTO.getRole());
                logger.error("🔍 Attempted to set invalid role: {}", roleUpdateDTO.getRole());
                logger.error("👤 Attempted by: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            } else {
                logger.error("❌ ENDPOINT RUNTIME ERROR: PUT /api/v1/users/{}/role - Runtime error: {}", userId, e.getMessage());
                logger.error("🔍 Runtime error type: {}", e.getClass().getSimpleName());
                logger.error("👤 Attempted by: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            }
            throw e;
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: PUT /api/v1/users/{}/role - Role update failed: {}", userId, e.getMessage());
            logger.error("🔍 Error type: {}", e.getClass().getSimpleName());
            logger.error("👤 Attempted by: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
            logger.error("📚 Stack trace: ", e);
            throw e;
        }
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user", description = "Deletes the current user's account")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        logger.info("🌐 ENDPOINT CALLED: DELETE /api/v1/users/me - Delete current user account");
        logger.info("⚠️  Self-deletion request from: {} (ID: {})", user.getEmail(), user.getId());
        
        try {
            userService.deleteUser(user.getId());
            
            logger.info("✅ ENDPOINT SUCCESS: DELETE /api/v1/users/me - Account deleted successfully");
            logger.info("🗑️  User account deleted: {} (ID: {})", user.getEmail(), user.getId());
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: DELETE /api/v1/users/me - Failed to delete account: {}", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user (requires ADMIN role)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        logger.info("🌐 ENDPOINT CALLED: DELETE /api/v1/users/{} - Delete user (ADMIN only)", userId);
        logger.info("⚠️  ADMIN deletion request for user ID: {}", userId);
        
        try {
            userService.deleteUser(userId);
            
            logger.info("✅ ENDPOINT SUCCESS: DELETE /api/v1/users/{} - User deleted successfully", userId);
            logger.info("🗑️  User deleted by ADMIN: ID {}", userId);
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: DELETE /api/v1/users/{} - Failed to delete user: {}", userId, e.getMessage());
            throw e;
        }
    }
} 