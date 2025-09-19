package com.library.management_system.DTOs;

import jakarta.validation.constraints.NotBlank;

public record MailingListDto(@NotBlank String email) {
}
