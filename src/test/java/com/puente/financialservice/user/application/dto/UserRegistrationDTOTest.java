package com.puente.financialservice.user.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationDTOTest {

    @Test
    void whenCreateUserRegistrationDTO_thenAllPropertiesAreSet() {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";

        // When
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO(name, email, password);

        // Then
        assertEquals(name, registrationDTO.getName());
        assertEquals(email, registrationDTO.getEmail());
        assertEquals(password, registrationDTO.getPassword());
    }

    @Test
    void whenSetProperties_thenPropertiesAreUpdated() {
        // Given
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();

        // When
        registrationDTO.setName("John Doe");
        registrationDTO.setEmail("john@example.com");
        registrationDTO.setPassword("password123");

        // Then
        assertEquals("John Doe", registrationDTO.getName());
        assertEquals("john@example.com", registrationDTO.getEmail());
        assertEquals("password123", registrationDTO.getPassword());
    }

    @Test
    void whenCreateEmptyUserRegistrationDTO_thenAllPropertiesAreNull() {
        // When
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();

        // Then
        assertNull(registrationDTO.getName());
        assertNull(registrationDTO.getEmail());
        assertNull(registrationDTO.getPassword());
    }
} 