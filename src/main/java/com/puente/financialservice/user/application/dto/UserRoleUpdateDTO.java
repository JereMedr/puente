package com.puente.financialservice.user.application.dto;

import com.puente.financialservice.user.domain.model.User.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateDTO {
    private UserRole role;
} 