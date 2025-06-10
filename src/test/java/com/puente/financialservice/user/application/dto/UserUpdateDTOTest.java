package com.puente.financialservice.user.application.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserUpdateDTOTest {

    @Test
    void whenSetProperties_thenPropertiesAreUpdated() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        // When
        updateDTO.setName("John Doe");
        updateDTO.setEmail("john@example.com");
        updateDTO.setPassword("newpassword123");

        // Then
        assertEquals("John Doe", updateDTO.getName());
        assertEquals("john@example.com", updateDTO.getEmail());
        assertEquals("newpassword123", updateDTO.getPassword());
    }

    @Test
    void whenCreateEmptyUserUpdateDTO_thenAllPropertiesAreNull() {
        // When
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        // Then
        assertNull(updateDTO.getName());
        assertNull(updateDTO.getEmail());
        assertNull(updateDTO.getPassword());
    }

    @Test
    void whenUpdatePartialProperties_thenOnlyUpdatedPropertiesChange() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        // When - update only name and email
        updateDTO.setName("John Doe");
        updateDTO.setEmail("john@example.com");

        // Then
        assertEquals("John Doe", updateDTO.getName());
        assertEquals("john@example.com", updateDTO.getEmail());
        assertNull(updateDTO.getPassword());
    }
} 