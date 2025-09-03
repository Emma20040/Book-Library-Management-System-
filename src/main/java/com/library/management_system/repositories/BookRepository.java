package com.library.management_system.repositories;




import com.library.management_system.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

// The JpaRepository takes two generic types:
// 1. The Entity type it works with (Book)
// 2. The type of the Entity's primary key (Long)
public interface BookRepository extends JpaRepository<Book, Long> {
    // You can also define custom methods here if you need more than the basic CRUD operations
    // For example: List<Book> findByAuthor(String author);
}

