package com.library.management_system.DTOs;

public record ProfileUpdateRequestDTO(
        String firstName,
        String lastName,
        String country,
        String address,
        String profileImageUrl,
        String bio,
        String phoneNumber
) {
}
