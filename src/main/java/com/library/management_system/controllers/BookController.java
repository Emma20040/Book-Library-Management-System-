package com.library.management_system.controllers;


import com.library.management_system.DTOs.BookRequestDTO;
import com.library.management_system.DTOs.BookResponseDTO;
import com.library.management_system.DTOs.PaginatedResponse;
import com.library.management_system.DTOs.UserProfileResponseDTO;
import com.library.management_system.models.Book;
import com.library.management_system.services.BookService;
import com.library.management_system.services.FileStorageService;
import com.library.management_system.services.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final FileStorageService fileStorageService;
    private final PaymentService paymentService;


    public BookController(BookService bookService, FileStorageService fileStorageService, PaymentService paymentService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
        this.paymentService = paymentService;
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
    public ResponseEntity<?> getAllBooks(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

//        create pageable with sorting by creation date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());


            Page<BookResponseDTO> books = bookService.getAllBooks(pageable);

//        Extract content and create paginated response which will remove uneccesary meta data
            List<BookResponseDTO> content = books.getContent();
//
            return ResponseEntity.ok(new PaginatedResponse<>(
                    content,
                    books.getTotalPages(),
                    books.getTotalElements()));

    }

//      Get a single book by ID
        @GetMapping("/bookDetails/{id}")
        public ResponseEntity<BookResponseDTO> getBookById (@PathVariable Long id){
            return bookService.getBookById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

//    Update an existing book with optional file updates
        @PutMapping(value = "/{id}", consumes = "multipart/form-data")
        public ResponseEntity<BookResponseDTO> updateBook (
                @PathVariable Long id,
                @RequestPart BookRequestDTO bookRequest,
                @RequestPart(value = "pdf", required = false) MultipartFile pdfFile,
                @RequestPart(value = "cover", required = false) MultipartFile coverImage){

            BookResponseDTO updatedBook = bookService.updateBook(id, bookRequest, pdfFile, coverImage);
            return ResponseEntity.ok(updatedBook);
        }


//      Delete a book and associated files
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBook (@PathVariable Long id){
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        }


//      Get book PDF file for reading for admins only
        @GetMapping("/pdf/{id}")
        public ResponseEntity<Resource> getBookPdf (@PathVariable Long id){
            Resource pdfResource = bookService.getBookPdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdfResource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfResource);
        }


//    checks book access and returns pdf url instead of pdf file
        @GetMapping("/read/url/{id}")
        public ResponseEntity<?> readBook (@PathVariable Long id){
            // Check if user has access to this book
            if (!paymentService.hasAccessToBook(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Get the book to retrieve PDF path
            BookResponseDTO book = bookService.getBookById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

            // Return the PDF path for the viewer instead of the file itself
            Map<String, String> response = new HashMap<>();
            response.put("pdfUrl", "/api/books/" + "/pdf-stream" + "/" + id);
            response.put("bookTitle", book.title());
            response.put("bookCover", book.coverImagePath());

            return ResponseEntity.ok(response);
        }


        // --- Secure PDF streaming for users which doesn't allow downloading---
        @GetMapping("/pdf-stream/{id}")
        public ResponseEntity<Resource> streamBookPdf (@PathVariable Long id,
                HttpServletResponse response){
            if (!paymentService.hasAccessToBook(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Resource pdfResource = bookService.getBookPdf(id);

            // Add headers to stop the broswer from caching and downloading
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    // Remove filename to make saving harder
                    .body(pdfResource);
        }


//      Get book cover image for display
        @GetMapping("/cover/{id}")
        public ResponseEntity<Resource> getBookCover (@PathVariable Long id){
            Resource imageResource = bookService.getBookCover(id);

            String filename = imageResource.getFilename();
            String contentType = fileStorageService.determineContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(imageResource);
        }

//    search book
        @GetMapping("/search")
        public ResponseEntity<?> getAllBooks (
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size,
        @RequestParam(value = "search", required = false) String search){
            Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
            Page<Book> bookPage;
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
                            book.getPublishedDate(),
                            book.getPricePerMonth(),
                            book.getAccessType(),
                            book.getNumberOfPages()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new PaginatedResponse<>(books, bookPage.getTotalPages(), bookPage.getTotalElements()));
        }


//    get the books by genre
        @GetMapping("/genre")
        public ResponseEntity<?> getBooksByGenre (
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size,
        @RequestParam(value = "genre", required = true) String genre){

            Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
            Page<Book> bookPage = bookService.getBooksByGenre(genre, pageable);
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
                            book.getPublishedDate(),
                            book.getPricePerMonth(),
                            book.getAccessType(),
                            book.getNumberOfPages()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new PaginatedResponse<>(books, bookPage.getTotalPages(), bookPage.getTotalElements()));

        }


//    get the total number of books
        @GetMapping("/countBooks")
        public ResponseEntity<Long> getTotalBooks () {
            Long totalBooks = bookService.countBooks();
            return ResponseEntity.ok(totalBooks);
        }


    }



    
