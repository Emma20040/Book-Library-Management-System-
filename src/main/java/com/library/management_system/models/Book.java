package com.library.management_system.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "cover_image_path")
    private String coverImagePath;


    //    / set columns to nullable to avoid migrations issues, which i am going to change later
    @Column(name = "created_at", nullable = true, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable =true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
