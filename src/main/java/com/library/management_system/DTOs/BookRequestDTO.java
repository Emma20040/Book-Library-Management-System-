package com.library.management_system.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BookRequestDTO(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        String description,
        String genre,
        String publishedDate

) {}