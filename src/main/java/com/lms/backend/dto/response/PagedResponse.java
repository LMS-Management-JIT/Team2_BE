package com.lms.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    private boolean hasNext;
    private boolean hasPrevious;

    private boolean first;
    private boolean last;

    /**
     * Constructor for backward compatibility with existing code
     */
    public PagedResponse(List<T> content, long totalElements, int page, int size, int totalPages) {
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;

        this.hasNext = (page + 1) < totalPages;
        this.hasPrevious = page > 0;
        this.first = page == 0;
        this.last = (page + 1) >= totalPages;
    }

    /**
     * Generic static factory method (Recommended)
     */
    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        return PagedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext((page + 1) < totalPages)
                .hasPrevious(page > 0)
                .first(page == 0)
                .last((page + 1) >= totalPages)
                .build();
    }
}