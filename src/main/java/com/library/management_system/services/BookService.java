package com.library.management_system.services;


import com.library.management_system.DTOs.BookRequestDTO;
import com.library.management_system.DTOs.BookResponseDTO;
import com.library.management_system.models.Book;
import com.library.management_system.repositories.BookRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository, FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.fileStorageService = fileStorageService;
    }



    public BookResponseDTO createBook(BookRequestDTO bookRequest, MultipartFile pdfFile, MultipartFile coverImage) {
        // Convert DTO to Entity
        Book book = new Book();
        book.setTitle(bookRequest.title());
        book.setAuthor(bookRequest.author());
        book.setIsbn(bookRequest.isbn());
        book.setDescription(bookRequest.description());
        book.setPublishedDate(bookRequest.publishedDate());
        book.setGenre(bookRequest.genre());


        // Store files
        String pdfFilename = generatePdfFilename(bookRequest.title(), pdfFile.getOriginalFilename());
        String pdfPath = fileStorageService.storePdf(pdfFile, pdfFilename);
        book.setPdfPath(pdfPath);

        String coverFilename = generateCoverFilename(bookRequest.title(), coverImage.getOriginalFilename());
        String coverPath = fileStorageService.storeCoverImage(coverImage, coverFilename);
        book.setCoverImagePath(coverPath);

        Book savedBook = bookRepository.save(book);
        return convertToDto(savedBook);
    }


    private BookResponseDTO convertToDto(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getDescription(),
                book.getPdfPath(),
                book.getCoverImagePath(),
                book.getPublishedDate(),
                book.getGenre()
        );
    }




//    get the pdf of the book
    public Resource getBookPdf(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return fileStorageService.loadPdf(fileStorageService.extractFilenameFromPath(book.getPdfPath()));
    }

//    get the image of the book cover
    public Resource getBookCover(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return fileStorageService.loadCoverImage(fileStorageService.extractFilenameFromPath(book.getCoverImagePath()));
    }


//    Update book information and optionally update PDF/cover files
public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequest, MultipartFile pdfFile, MultipartFile coverImage) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

    // Update basic book information
    if (bookRequest.title() != null) book.setTitle(bookRequest.title());
    if (bookRequest.author() != null) book.setAuthor(bookRequest.author());
    if (bookRequest.isbn() != null) book.setIsbn(bookRequest.isbn());
    if (bookRequest.description() != null) book.setDescription(bookRequest.description());
    if (bookRequest.publishedDate() != null) book.setPublishedDate(bookRequest.publishedDate());
    if (bookRequest.genre() != null) book.setGenre(bookRequest.genre());


    // Update PDF file if provided
    if (pdfFile != null && !pdfFile.isEmpty()) {
            // Delete old PDF file if exists
        if (book.getPdfPath() != null) {
                fileStorageService.deleteFile(book.getPdfPath());
            }
            String pdfFilename = generatePdfFilename(book.getTitle(), pdfFile.getOriginalFilename());
            String pdfPath = fileStorageService.storePdf(pdfFile, pdfFilename);
            book.setPdfPath(pdfPath);
        }

        // Update cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            // Delete old cover image if exists
            if (book.getCoverImagePath() != null) {
                fileStorageService.deleteFile(book.getCoverImagePath());
            }
            String coverFilename = generateCoverFilename(book.getTitle(), coverImage.getOriginalFilename());
            String coverPath = fileStorageService.storeCoverImage(coverImage, coverFilename);
            book.setCoverImagePath(coverPath);
        }

    Book updatedBook = bookRepository.save(book);
    return convertToDto(updatedBook);
    }

//    Get all books

    public List<BookResponseDTO> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToDto)
                .toList();
    }


//      Get book by ID
    public Optional<BookResponseDTO> getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::convertToDto);
    }


    //    Delete book and all associated files
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // Delete associated files from storage
        if (book.getPdfPath() != null) {
            fileStorageService.deleteFile(book.getPdfPath());
        }
        if (book.getCoverImagePath() != null) {
            fileStorageService.deleteFile(book.getCoverImagePath());
        }

        // Delete book from database
        bookRepository.delete(book);
    }


//    generate pdf filename so that it is unique
    private String generatePdfFilename(String bookTitle, String originalFilename) {
        String cleanTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return cleanTitle + "-" + System.currentTimeMillis() + extension;
    }


//    generate cover image filename so that it is unique
    private String generateCoverFilename(String bookTitle, String originalFilename) {
        String cleanTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return cleanTitle + "-cover-" + System.currentTimeMillis() + extension;
    }

//    search book by title or author or genre with recomadation if search doesn't exist
public Page<Book> primarySearch(String searchWord, Pageable pageable) {
    try {
        return bookRepository.primarySearch(searchWord, pageable);
    } catch (Exception e) {
        // Fallback to recommendation search if primary search is not found
        return bookRepository.recommendationSearch(searchWord, pageable);
    }
}
}