package com.puente.financialservice.user.application.service;

import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import com.puente.financialservice.user.application.dto.UserDTO;
import com.puente.financialservice.user.application.dto.UserRegistrationDTO;
import com.puente.financialservice.user.application.dto.UserRoleUpdateDTO;
import com.puente.financialservice.user.application.dto.UserUpdateDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(User.UserRole.USER);

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    public UserDTO updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null) {
            if (!user.getEmail().equals(updateDTO.getEmail()) && 
                userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public UserDTO updateUserRole(Long userId, UserRoleUpdateDTO roleUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(roleUpdateDTO.getRole());
        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
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