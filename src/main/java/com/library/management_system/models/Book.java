package com.library.management_system.models;



import com.library.management_system.enums.BookAccessType;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity // Marks this class as a JPA entity, mapped to a table in the database
@Data // From Lombok, automatically generates getters, setters, toString, etc.
@NoArgsConstructor // From Lombok, generates a no-argument constructor
@AllArgsConstructor // From Lombok, generates a constructor with all arguments
public class Book {

    @Id // Marks this field as the primary key of the table
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tells JPA to auto-generate the ID
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private String publishedDate;

    private String description;
    private String genre;
    private int numberOfPages;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    @Column(name="price_per_month", nullable = false)
    private BigDecimal pricePerMonth;


    //    / set columns to nullable to avoid migrations issues, which i am going to change later
    @Column(name = "created_at", nullable = true, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable =true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private BookAccessType accessType;



    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (this.accessType == null) {
            if (this.pricePerMonth != null && this.pricePerMonth.compareTo(BigDecimal.ZERO) == 0) {
                this.accessType = BookAccessType.FREE;
            } else {
                this.accessType = BookAccessType.PAID;
            }
        }
    }




}
