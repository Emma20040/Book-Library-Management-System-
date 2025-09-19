package com.library.management_system.DTOs;

import jakarta.validation.constraints.NotBlank;

public record ContactUsDto(
        String firstName,
        String lastName,
        String subject,
        @NotBlank String email,
        @NotBlank String message

) {
}
