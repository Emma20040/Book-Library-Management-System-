package com.library.management_system.DTOs;

import java.util.List;

// Helper class for paginated response
public  class PaginatedResponse<T> {
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
