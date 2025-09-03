package com.dev_squad.book_library_backend.controller;

import com.dev_squad.book_library_backend.model.Book;
import com.dev_squad.book_library_backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import for HTTP status codes
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/books") // Base URL for all endpoints in this controller
public class BookController {

    @Autowired // Spring will automatically inject an instance of BookRepository
    private BookRepository bookRepository;

    // CREATE a new book (POST request)
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookRepository.save(book);
        // Corrected: Return a ResponseEntity with the created book and 201 Created status
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    // READ all books (GET request)
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // READ a single book by ID (GET request with a path variable)
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> ResponseEntity.ok(book))
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE an existing book (PUT request)
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(updatedBook.getTitle());
                    book.setAuthor(updatedBook.getAuthor());
                    book.setIsbn(updatedBook.getIsbn());
                    Book savedBook = bookRepository.save(book);
                    return ResponseEntity.ok(savedBook);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE a book (DELETE request)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
