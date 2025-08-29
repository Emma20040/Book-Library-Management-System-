package com.library.management_system.DTOs;

import jakarta.validation.constraints.Email;

public record UserRedeemPasswordDto(@Email String email) {
}
