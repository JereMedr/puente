package com.puente.financialservice.user.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void whenCreateUserDTO_thenAllPropertiesAreSet() {
        // Given
        Long id = 1L;
        String name = "John Doe";
        String email = "john@example.com";
        String role = "USER";

        // When
        UserDTO userDTO = new UserDTO(id, name, email, role);

        // Then
        assertEquals(id, userDTO.getId());
        assertEquals(name, userDTO.getName());
        assertEquals(email, userDTO.getEmail());
        assertEquals(role, userDTO.getRole());
    }

    @Test
    void whenSetProperties_thenPropertiesAreUpdated() {
        // Given
        UserDTO userDTO = new UserDTO();

        // When
        userDTO.setId(1L);
        userDTO.setName("John Doe");
        userDTO.setEmail("john@example.com");
        userDTO.setRole("USER");

        // Then
        assertEquals(1L, userDTO.getId());
        assertEquals("John Doe", userDTO.getName());
        assertEquals("john@example.com", userDTO.getEmail());
        assertEquals("USER", userDTO.getRole());
    }

    @Test
    void whenCreateEmptyUserDTO_thenAllPropertiesAreNull() {
        // When
        UserDTO userDTO = new UserDTO();

        // Then
        assertNull(userDTO.getId());
        assertNull(userDTO.getName());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getRole());
    }
} 