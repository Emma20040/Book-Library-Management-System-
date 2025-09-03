package com.library.management_system.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}
