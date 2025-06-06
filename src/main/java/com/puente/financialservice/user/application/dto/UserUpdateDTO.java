package com.puente.financialservice.user.application.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String name;
    private String email;
    private String password;
} 