package com.puente.financialservice.user.infrastructure.controller;

import com.puente.financialservice.user.application.dto.UserDTO;
import com.puente.financialservice.user.application.dto.UserRoleUpdateDTO;
import com.puente.financialservice.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Updates the role of a user (requires ADMIN role)")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateDTO roleUpdateDTO) {
        return ResponseEntity.ok(userService.updateUserRole(userId, roleUpdateDTO));
    }
} 