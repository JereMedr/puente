package com.puente.financialservice.user.infrastructure.security;

import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("🔐 JWT Authentication Filter initialized");
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("🔐 JWT Filter processing: {} {}", method, requestPath);
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("❌ No Bearer token found for {} {} - proceeding without authentication", method, requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String maskedToken = jwt.length() > 10 ? jwt.substring(0, 10) + "..." + jwt.substring(jwt.length() - 4) : "***";
        logger.info("🔑 JWT Token received for {} {}: {}", method, requestPath, maskedToken);
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            Long userId = claims.get("userId", Long.class);
            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();

            logger.info("📋 JWT Claims parsed successfully:");
            logger.info("   👤 Email: {}", email);
            logger.info("   🏷️  Role: {}", role);
            logger.info("   🆔 User ID: {}", userId);
            logger.info("   ⏰ Issued at: {}", issuedAt);
            logger.info("   📅 Expires at: {}", expiration);
            
            // Check if token is expired
            if (expiration.before(new Date())) {
                logger.warn("⚠️ Token expired for user: {} - clearing security context", email);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("❌ User not found in database: {}", email);
                        return new RuntimeException("User not found");
                    });

            logger.info("✅ User found in database: ID={}, Name={}, Role={}", 
                user.getId(), user.getName(), user.getRole());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            
            logger.info("🎯 Authentication successful for {} {} - User: {}, Role: ROLE_{}", 
                method, requestPath, email, role);
            
        } catch (JwtException e) {
            logger.error("❌ JWT validation failed for {} {}: {} - {}", 
                method, requestPath, e.getClass().getSimpleName(), e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            logger.error("💥 Unexpected error during JWT processing for {} {}: {}", 
                method, requestPath, e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
} 