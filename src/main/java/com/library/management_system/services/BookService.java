package com.library.management_system.services;


import com.library.management_system.models.Book;
import com.library.management_system.repositories.BookRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository, FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.fileStorageService = fileStorageService;
    }

    public Book createBook(Book book, MultipartFile pdfFile, MultipartFile coverImage) {
        // Store PDF
        String pdfFilename = generatePdfFilename(book.getTitle(), pdfFile.getOriginalFilename());
        String pdfPath = fileStorageService.storePdf(pdfFile, pdfFilename);
        book.setPdfPath(pdfPath);

        // Store cover image
        String coverFilename = generateCoverFilename(book.getTitle(), coverImage.getOriginalFilename());
        String coverPath = fileStorageService.storeCoverImage(coverImage, coverFilename);
        book.setCoverImagePath(coverPath);

        return bookRepository.save(book);
    }

    public Resource getBookPdf(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return fileStorageService.loadPdf(fileStorageService.extractFilenameFromPath(book.getPdfPath()));
    }

    public Resource getBookCover(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return fileStorageService.loadCoverImage(fileStorageService.extractFilenameFromPath(book.getCoverImagePath()));
    }

    private String generatePdfFilename(String bookTitle, String originalFilename) {
        String cleanTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return cleanTitle + "-" + System.currentTimeMillis() + extension;
    }

    private String generateCoverFilename(String bookTitle, String originalFilename) {
        String cleanTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return cleanTitle + "-cover-" + System.currentTimeMillis() + extension;
    }
}