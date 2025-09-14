package com.library.management_system.repositories;

import com.library.management_system.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;



import java.util.List;


// The JpaRepository takes two generic types:
// 1. The Entity type it works with (Book)
// 2. The type of the Entity's primary key (Long)
public interface BookRepository extends JpaRepository<Book, Long> {

//    these two methods are already in use by JPA
//    List<Book> findAll();
//    List<Book> finById(Long id);

    // You can also define custom methods here if you need more than the basic CRUD operations
    // For example: List<Book> findByAuthor(String author);

    @Query(value = "SELECT * FROM book WHERE " +
            "LOWER(title) LIKE LOWER(CONCAT('%', :searchWord, '%')) OR " +
            "LOWER(author) LIKE LOWER(CONCAT('%', :searchWord, '%')) OR " +
            "LOWER(genre) LIKE LOWER(CONCAT('%', :searchWord, '%')) OR " +
            "LOWER(isbn) LIKE LOWER(CONCAT('%', :searchWord, '%'))",
            nativeQuery = true)
    Page<Book> primarySearch(@Param("searchWord") String searchWord, Pageable pageable);

    @Query(value = "SELECT * FROM book WHERE " +
            "SOUNDEX(title) = SOUNDEX(:searchWord) OR " +
            "SOUNDEX(author) = SOUNDEX(:searchWord) OR " +
            "SOUNDEX(genre) = SOUNDEX(:searchWord) OR " +
            "LOWER(title) LIKE LOWER(CONCAT('%', :searchWord, '%')) OR " +
            "LOWER(author) LIKE LOWER(CONCAT('%', :searchWord, '%')) OR " +
            "LOWER(genre) LIKE LOWER(CONCAT('%', :searchWord, '%'))",
            nativeQuery = true)
    Page<Book> recommendationSearch(@Param("searchWord") String searchWord, Pageable pageable);

//    find book by genre
    Page<Book> findByGenre(String genre, Pageable pageable);

    long count();
}


