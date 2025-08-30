package com.library.management_system.DTOs;

import com.library.management_system.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponseDTO(
        UUID id,
        String username,
        String email,
        Role role,
        String firstName,
        String lastName,
        String country,
        String address,
        String profileImageUrl,
        String bio,
        String phoneNumber
) {
    // Record automatically generates everything
}
