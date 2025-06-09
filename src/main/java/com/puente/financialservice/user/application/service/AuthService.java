package com.puente.financialservice.user.application.service;

import com.puente.financialservice.user.application.dto.LoginDTO;
import com.puente.financialservice.user.application.dto.UserRegistrationDTO;
import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long expirationTime;

    @PostConstruct
    public void init() {
        logger.info("🔐 AuthService initialized:");
        logger.info("   🕐 JWT Expiration: {} ms ({} hours)", expirationTime, expirationTime / (1000 * 60 * 60));
        String maskedSecret = jwtSecret != null && jwtSecret.length() > 8 ? 
            jwtSecret.substring(0, 4) + "***" + jwtSecret.substring(jwtSecret.length() - 4) : "***";
        logger.info("   🔑 JWT Secret loaded: {}", maskedSecret);
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<String, String> login(LoginDTO loginDTO) {
        logger.info("🔐 LOGIN ATTEMPT:");
        logger.info("   📧 Email: {}", loginDTO.getEmail());
        logger.info("   ⏰ Timestamp: {}", LocalDateTime.now().format(formatter));
        
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    logger.warn("❌ LOGIN FAILED: User not found for email: {}", loginDTO.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        logger.info("✅ User found in database:");
        logger.info("   🆔 ID: {}", user.getId());
        logger.info("   👤 Name: {}", user.getName());
        logger.info("   🏷️  Role: {}", user.getRole());

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            logger.warn("❌ LOGIN FAILED: Invalid password for user: {}", loginDTO.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        logger.info("✅ Password validation successful");
        
        Date issuedAt = new Date();
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);
        
        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(getSigningKey())
                .compact();

        String maskedToken = token.length() > 20 ? 
            token.substring(0, 10) + "..." + token.substring(token.length() - 10) : "***";

        logger.info("🎯 JWT TOKEN GENERATED:");
        logger.info("   🔑 Token: {}", maskedToken);
        logger.info("   📅 Issued at: {}", issuedAt);
        logger.info("   ⏰ Expires at: {}", expirationDate);
        logger.info("   🕐 Valid for: {} hours", expirationTime / (1000 * 60 * 60));

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        
        logger.info("🎉 LOGIN SUCCESSFUL for user: {} ({})", user.getName(), user.getEmail());
        
        return response;
    }
} 