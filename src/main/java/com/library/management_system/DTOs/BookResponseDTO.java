package com.library.management_system.DTOs;


import java.math.BigDecimal;

public record BookResponseDTO(
        Long id,
        String title,
        String author,
        String isbn,
        String description,
        String pdfPath,
        String coverImagePath,
        String genre,
        String publishedDate
) {}