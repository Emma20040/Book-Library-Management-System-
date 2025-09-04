package com.library.management_system.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// Custom exception class for file storage errors
class FileStorageException extends RuntimeException {
    private final HttpStatus status;

    public FileStorageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public FileStorageException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

@Service
public class FileStorageService {
    private final Path pdfStorageLocation;
    private final Path coverImageStorageLocation;
    public final Path profilePicturesLocation;

    public FileStorageService() {
        this.pdfStorageLocation = Paths.get("uploads/books/pdfs").toAbsolutePath().normalize();
        this.coverImageStorageLocation = Paths.get("uploads/books/coverImages").toAbsolutePath().normalize();
        this.profilePicturesLocation = Paths.get("uploads/users/profilePictures").toAbsolutePath().normalize();

        try {
            Files.createDirectories(pdfStorageLocation);
            Files.createDirectories(coverImageStorageLocation);
            Files.createDirectories(profilePicturesLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Validating files
    public void validateFile(MultipartFile file, long maxSize, String[] allowedTypes) {
        if (file.isEmpty()) {
            throw new FileStorageException("File is empty", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxSize) {
            throw new FileStorageException("File is too large", HttpStatus.BAD_REQUEST);
        }
        if (allowedTypes != null && allowedTypes.length > 0) {
            String contentType = file.getContentType();
            boolean validType = false;
            for (String type : allowedTypes) {
                if (contentType != null && contentType.startsWith(type)) {
                    validType = true;
                    break;
                }
            }

            if (!validType) {
                throw new FileStorageException("Invalid file type. Allowed: " + String.join(", ", allowedTypes), HttpStatus.BAD_REQUEST);
            }
        }
    }

    public String extractFilenameFromPath(String path) {
        return Paths.get(path).getFileName().toString();
    }

    // PDF methods
    public String storePdf(MultipartFile file, String filename) {
        validateFile(file, 100 * 1024 * 1024, new String[]{"application/pdf"});
        return storeFile(file, pdfStorageLocation, filename);
    }

    public Resource loadPdf(String filename) {
        return loadFile(pdfStorageLocation, filename);
    }

    // Book Cover Methods
    public String storeCoverImage(MultipartFile file, String filename) {
        validateFile(file, 10 * 1024 * 1024, new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"});
        return storeFile(file, coverImageStorageLocation, filename);
    }

    public Resource loadCoverImage(String filename) {
        return loadFile(coverImageStorageLocation, filename);
    }

    public String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }

    // Profile Picture Methods
    public String storeProfilePicture(MultipartFile file, String filename) {
        validateFile(file, 5 * 1024 * 1024, new String[]{"image/jpeg", "image/png", "image/gif"});
        return storeFile(file, profilePicturesLocation, filename);
    }

    public Resource loadProfilePicture(String filename) {
        return loadFile(profilePicturesLocation, filename);
    }


    // Generic file handling
    private String storeFile(MultipartFile file, Path location, String filename) {
        try {
            if (filename.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + filename, HttpStatus.BAD_REQUEST);
            }

            Path targetLocation = location.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException e) {
            throw new FileStorageException("Could not store file: " + filename, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    how files will be loaded
    private Resource loadFile(Path location, String filename) {
        try {
            Path filePath = location.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filename, HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("File not found: " + filename, e, HttpStatus.NOT_FOUND);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + filePath, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}