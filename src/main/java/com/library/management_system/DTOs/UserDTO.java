package com.library.management_system.DTOs;

import jakarta.validation.constraints.*;

public record UserDTO(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email @NotBlank String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters")
//        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
//                                           message = "Password must contain at least one uppercase, one lowercase, and one number")
                String password
) {
}
