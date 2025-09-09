package com.library.management_system.controllers;


import com.library.management_system.DTOs.BookRequestDTO;
import com.library.management_system.DTOs.BookResponseDTO;
import com.library.management_system.services.BookService;
import com.library.management_system.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final FileStorageService fileStorageService;



    public BookController(BookService bookService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
    }


//    Create a new book with PDF and cover image
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BookResponseDTO> createBook(
            @RequestPart BookRequestDTO bookRequest,
            @RequestPart("pdf") MultipartFile pdfFile,
            @RequestPart("cover") MultipartFile coverImage) {

        BookResponseDTO savedBook = bookService.createBook(bookRequest, pdfFile, coverImage);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }


//      Get all books

    @GetMapping
    public List<BookResponseDTO> getAllBooks() {

        return bookService.getAllBooks();
    }


//      Get a single book by ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    Update an existing book with optional file updates
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Long id,
            @RequestPart BookRequestDTO bookRequest,
            @RequestPart(value = "pdf", required = false) MultipartFile pdfFile,
            @RequestPart(value = "cover", required = false) MultipartFile coverImage) {

        BookResponseDTO updatedBook = bookService.updateBook(id, bookRequest, pdfFile, coverImage);
        return ResponseEntity.ok(updatedBook);
    }


//      Delete a book and associated files
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }


//      Get book PDF file for reading
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getBookPdf(@PathVariable Long id) {
        Resource pdfResource = bookService.getBookPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfResource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfResource);
    }


//      Get book cover image for display
    @GetMapping("/{id}/cover")
    public ResponseEntity<Resource> getBookCover(@PathVariable Long id) {
        Resource imageResource = bookService.getBookCover(id);

        String filename = imageResource.getFilename();
        String contentType = fileStorageService.determineContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageResource);
    }

//    search book

    @GetMapping("/search")
    public ResponseEntity<?> getAllBooks(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<com.library.management_system.models.Book> bookPage;
        if (search != null && !search.isEmpty()) {
            bookPage = bookService.primarySearch(search, pageable);
        } else {
            bookPage = Page.empty(pageable);
        }
        List<BookResponseDTO> books = bookPage.getContent().stream()
                .map(book -> new BookResponseDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getDescription(),
                        book.getPdfPath(),
                        book.getCoverImagePath(),
                        book.getGenre(),
                        book.getPublishedDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PaginatedResponse<>(books, bookPage.getTotalPages(), bookPage.getTotalElements()));
    }


    // Helper class for paginated response
    public static class PaginatedResponse<T> {
        private List<T> content;
        private int totalPages;
        private long totalElements;

        public PaginatedResponse(List<T> content, int totalPages, long totalElements) {
            this.content = content;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }

        public List<T> getContent() {
            return content;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }
    }
}


    
