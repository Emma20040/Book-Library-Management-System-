package com.library.management_system.DTOs;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Email or username is required")
        String emailOrUsername,

        @NotBlank(message = "Password is required")
        String password
) {}
