package com.library.management_system.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserResetPasswordDto(@NotBlank String token,
//                                   @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
//                                           message = "Password must contain at least one uppercase, one lowercase, and one number")
                                   @NotBlank
                                   @Size(min = 8,  message = "Password must be at least 8 characters") String password) {
}
