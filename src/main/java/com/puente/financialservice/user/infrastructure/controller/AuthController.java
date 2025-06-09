package com.puente.financialservice.user.infrastructure.controller;

import com.puente.financialservice.user.application.dto.LoginDTO;
import com.puente.financialservice.user.application.dto.UserDTO;
import com.puente.financialservice.user.application.dto.UserRegistrationDTO;
import com.puente.financialservice.user.application.service.AuthService;
import com.puente.financialservice.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    public ResponseEntity<UserDTO> register(@RequestBody UserRegistrationDTO registrationDTO) {
        logger.info("🌐 ENDPOINT CALLED: POST /api/v1/auth/register - User registration");
        logger.info("📝 Registration attempt for email: {}", registrationDTO.getEmail());
        
        try {
            UserDTO registeredUser = userService.registerUser(registrationDTO);
            
            logger.info("✅ ENDPOINT SUCCESS: POST /api/v1/auth/register - User registered successfully");
            logger.info("🎉 New user created: ID={}, Name={}, Email={}", 
                registeredUser.getId(), registeredUser.getName(), registeredUser.getEmail());
            
            return ResponseEntity.ok(registeredUser);
            
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: POST /api/v1/auth/register - Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        logger.info("🌐 ENDPOINT CALLED: POST /api/v1/auth/login - User authentication");
        logger.info("🔐 Login attempt for email: {}", loginDTO.getEmail());
        
        try {
            Map<String, String> authResponse = authService.login(loginDTO);
            
            // Extract token info for logging (safely)
            String token = authResponse.get("token");
            String maskedToken = token != null && token.length() > 20 ? 
                token.substring(0, 10) + "..." + token.substring(token.length() - 10) : "***";
            
            logger.info("✅ ENDPOINT SUCCESS: POST /api/v1/auth/login - Authentication successful");
            logger.info("🎯 JWT token generated for user: {} - Token: {}", loginDTO.getEmail(), maskedToken);
            
            return ResponseEntity.ok(authResponse);
            
        } catch (Exception e) {
            logger.error("❌ ENDPOINT ERROR: POST /api/v1/auth/login - Authentication failed for {}: {}", 
                loginDTO.getEmail(), e.getMessage());
            throw e;
        }
    }
} 