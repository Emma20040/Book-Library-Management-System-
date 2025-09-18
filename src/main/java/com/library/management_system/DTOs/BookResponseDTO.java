package com.library.management_system.DTOs;


import com.library.management_system.enums.BookAccessType;

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
        String publishedDate,
        BigDecimal pricePerMonth,
        BookAccessType accessType,
        int numberOfPages
) {}