package com.puente.financialservice.user.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void whenCreateUser_thenAllPropertiesAreSet() {
        // Given
        Long id = 1L;
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";
        User.UserRole role = User.UserRole.USER;

        // When
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        // Then
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void whenCreateUserWithAllArgsConstructor_thenAllPropertiesAreSet() {
        // Given
        Long id = 1L;
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";
        User.UserRole role = User.UserRole.USER;

        // When
        User user = new User(id, name, email, password, role);

        // Then
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void whenCreateUserRole_thenCorrectValuesExist() {
        // Then
        assertNotNull(User.UserRole.USER);
        assertNotNull(User.UserRole.ADMIN);
        assertNotEquals(User.UserRole.USER, User.UserRole.ADMIN);
    }
} 