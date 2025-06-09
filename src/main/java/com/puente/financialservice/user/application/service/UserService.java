package com.puente.financialservice.user.application.service;

import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import com.puente.financialservice.user.application.dto.UserDTO;
import com.puente.financialservice.user.application.dto.UserRegistrationDTO;
import com.puente.financialservice.user.application.dto.UserRoleUpdateDTO;
import com.puente.financialservice.user.application.dto.UserUpdateDTO;
import com.puente.financialservice.favorite.domain.port.FavoriteRepository;
import com.puente.financialservice.favorite.domain.model.Favorite;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FavoriteRepository favoriteRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FavoriteRepository favoriteRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.favoriteRepository = favoriteRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("👥 UserService initialized");
        try {
            List<User> users = userRepository.findAll();
            logger.info("📊 Current users in database: {}", users.size());
        } catch (Exception e) {
            logger.info("📊 UserService ready (unable to count users at startup)");
        }
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId().equals(userId);
    }

    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        logger.info("👤 USER REGISTRATION:");
        logger.info("   📧 Email: {}", registrationDTO.getEmail());
        logger.info("   👤 Name: {}", registrationDTO.getName());
        
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            logger.warn("❌ REGISTRATION FAILED: Email already exists: {}", registrationDTO.getEmail());
            throw new RuntimeException("Email already exists");
        }

        logger.info("✅ Email validation passed - email is available");
        
        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(registrationDTO.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(User.UserRole.USER);

        logger.info("🔐 Password encoded successfully");
        logger.info("🏷️  Default role assigned: {}", User.UserRole.USER);

        User savedUser = userRepository.save(user);
        
        logger.info("💾 User saved to database:");
        logger.info("   🆔 Generated ID: {}", savedUser.getId());
        logger.info("   👤 Name: {}", savedUser.getName());
        logger.info("   📧 Email: {}", savedUser.getEmail());
        logger.info("   🏷️  Role: {}", savedUser.getRole());
        
        UserDTO result = mapToDTO(savedUser);
        
        logger.info("🎉 USER REGISTRATION SUCCESSFUL for: {}", savedUser.getEmail());
        
        return result;
    }

    public List<UserDTO> getAllUsers() {
        logger.info("👥 GET ALL USERS request");
        
        try {
            List<User> users = userRepository.findAll();
            
            logger.info("📊 Found {} users in database", users.size());
            logger.info("👤 User summary: {}", 
                users.stream()
                    .map(u -> u.getEmail() + "(" + u.getRole() + ")")
                    .collect(Collectors.toList()));
            
            List<UserDTO> result = users.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            
            logger.info("✅ All users retrieved successfully");
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve all users: {}", e.getMessage());
            throw e;
        }
    }

    public UserDTO getUserById(Long userId) {
        logger.info("🔍 GET USER BY ID request: {}", userId);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found with ID: {}", userId);
                        return new RuntimeException("User not found");
                    });
            
            logger.info("✅ User found: ID={}, Name={}, Email={}, Role={}", 
                user.getId(), user.getName(), user.getEmail(), user.getRole());
            
            UserDTO result = mapToDTO(user);
            
            logger.info("🎯 User retrieved successfully: {}", user.getEmail());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to get user by ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public UserDTO getUserByEmail(String email) {
        logger.info("🔍 GET USER BY EMAIL request: {}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found with email: {}", email);
                        return new RuntimeException("User not found");
                    });
            
            logger.info("✅ User found: ID={}, Name={}, Email={}, Role={}", 
                user.getId(), user.getName(), user.getEmail(), user.getRole());
            
            UserDTO result = mapToDTO(user);
            
            logger.info("🎯 User retrieved successfully by email: {}", email);
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to get user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    public UserDTO updateUser(Long userId, UserUpdateDTO updateDTO) {
        logger.info("📝 UPDATE USER request: ID={}", userId);
        logger.info("🔄 Update data: Name={}, Email={}, Password={}", 
            updateDTO.getName(), 
            updateDTO.getEmail(),
            updateDTO.getPassword() != null ? "[PROVIDED]" : "[NOT PROVIDED]");
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found for update: ID={}", userId);
                        return new RuntimeException("User not found");
                    });

            logger.info("👤 Current user data: Name={}, Email={}, Role={}", 
                user.getName(), user.getEmail(), user.getRole());

            if (updateDTO.getName() != null) {
                String oldName = user.getName();
                user.setName(updateDTO.getName());
                logger.info("✏️  Name updated: '{}' → '{}'", oldName, updateDTO.getName());
            }
            
            if (updateDTO.getEmail() != null) {
                if (!user.getEmail().equals(updateDTO.getEmail()) && 
                    userRepository.existsByEmail(updateDTO.getEmail())) {
                    logger.warn("❌ Email already exists: {}", updateDTO.getEmail());
                    throw new RuntimeException("Email already exists");
                }
                String oldEmail = user.getEmail();
                user.setEmail(updateDTO.getEmail());
                logger.info("📧 Email updated: '{}' → '{}'", oldEmail, updateDTO.getEmail());
            }
            
            if (updateDTO.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
                logger.info("🔐 Password updated and encoded");
            }

            User updatedUser = userRepository.save(user);
            
            logger.info("💾 User saved successfully: ID={}, Name={}, Email={}", 
                updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
            
            UserDTO result = mapToDTO(updatedUser);
            
            logger.info("🎉 USER UPDATE SUCCESSFUL for: {}", updatedUser.getEmail());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to update user ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public UserDTO updateUserByEmail(String email, UserUpdateDTO updateDTO) {
        logger.info("📝 UPDATE USER BY EMAIL request: {}", email);
        logger.info("🔄 Update data: Name={}, Email={}, Password={}", 
            updateDTO.getName(), 
            updateDTO.getEmail(),
            updateDTO.getPassword() != null ? "[PROVIDED]" : "[NOT PROVIDED]");
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found for update by email: {}", email);
                        return new RuntimeException("User not found");
                    });

            logger.info("👤 Current user data: ID={}, Name={}, Email={}, Role={}", 
                user.getId(), user.getName(), user.getEmail(), user.getRole());

            if (updateDTO.getName() != null) {
                String oldName = user.getName();
                user.setName(updateDTO.getName());
                logger.info("✏️  Name updated: '{}' → '{}'", oldName, updateDTO.getName());
            }
            
            if (updateDTO.getEmail() != null) {
                if (!user.getEmail().equals(updateDTO.getEmail()) && 
                    userRepository.existsByEmail(updateDTO.getEmail())) {
                    logger.warn("❌ Email already exists: {}", updateDTO.getEmail());
                    throw new RuntimeException("Email already exists");
                }
                String oldEmail = user.getEmail();
                user.setEmail(updateDTO.getEmail());
                logger.info("📧 Email updated: '{}' → '{}'", oldEmail, updateDTO.getEmail());
            }
            
            if (updateDTO.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
                logger.info("🔐 Password updated and encoded");
            }

            User updatedUser = userRepository.save(user);
            
            logger.info("💾 User updated successfully by email: ID={}, Name={}, Email={}", 
                updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
            
            UserDTO result = mapToDTO(updatedUser);
            
            logger.info("🎉 USER UPDATE BY EMAIL SUCCESSFUL for: {}", updatedUser.getEmail());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to update user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    public UserDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO) {
        logger.info("🏷️  UPDATE USER ROLE request: ID={}", userId);
        logger.info("🔄 Role change: New role = {}", roleUpdateDTO.getRole());
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found for role update: ID={}", userId);
                        return new RuntimeException("User not found");
                    });
            
            User.UserRole oldRole = user.getRole();
            logger.info("👤 Current user: Name={}, Email={}, Current Role={}", 
                user.getName(), user.getEmail(), oldRole);
            
            user.setRole(roleUpdateDTO.getRole());
            
            logger.info("🔄 Role changing: {} → {}", oldRole, roleUpdateDTO.getRole());
            
            User updatedUser = userRepository.save(user);
            
            logger.info("💾 User role updated successfully: ID={}, Email={}, New Role={}", 
                updatedUser.getId(), updatedUser.getEmail(), updatedUser.getRole());
            
            UserDTO result = mapToDTO(updatedUser);
            
            logger.info("🎉 USER ROLE UPDATE SUCCESSFUL: {} now has role {}", 
                updatedUser.getEmail(), updatedUser.getRole());
            
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to update role for user ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public void deleteUser(Long userId) {
        logger.info("🗑️  DELETE USER request: ID={}", userId);
        
        try {
            User userToDelete = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found for deletion: ID={}", userId);
                        return new RuntimeException("User not found");
                    });
            
            logger.info("⚠️  About to delete user: Name={}, Email={}, Role={}", 
                userToDelete.getName(), userToDelete.getEmail(), userToDelete.getRole());

            // Obtener y eliminar todos los favoritos del usuario
            List<Favorite> userFavorites = favoriteRepository.findAllByUser(userToDelete);
            logger.info("📊 Found {} favorites to delete for user ID={}", userFavorites.size(), userId);

            for (Favorite favorite : userFavorites) {
                logger.info("🗑️  Deleting favorite: Symbol={} for user={}", favorite.getSymbol(), userToDelete.getEmail());
                favoriteRepository.delete(favorite);
            }
            
            logger.info("✅ All favorites deleted for user ID={}", userId);
            
            // Ahora sí eliminamos el usuario
            userRepository.deleteById(userId);
            
            logger.info("✅ User deleted successfully: ID={}", userId);
            logger.info("🎯 Deleted user was: {}", userToDelete.getEmail());
            
        } catch (Exception e) {
            logger.error("❌ Failed to delete user ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    public void deleteUserByEmail(String email) {
        logger.info("🗑️  DELETE USER BY EMAIL request: {}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("❌ User not found for deletion by email: {}", email);
                        return new RuntimeException("User not found");
                    });
            
            logger.info("⚠️  About to delete user by email: ID={}, Name={}, Email={}, Role={}", 
                user.getId(), user.getName(), user.getEmail(), user.getRole());
            
            userRepository.deleteById(user.getId());
            
            logger.info("✅ User deleted successfully by email: {}", email);
            logger.info("🎯 Deleted user was: ID={}, Name={}", user.getId(), user.getName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to delete user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    private UserDTO mapToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
    }
} 