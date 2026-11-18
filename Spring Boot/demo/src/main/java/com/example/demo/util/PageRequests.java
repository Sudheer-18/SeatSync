package com.example.demo.util;

import com.example.demo.exception.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequests {
    private PageRequests() { }

    public static PageRequest of(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > 50) {
            throw ApiException.badRequest("page must be non-negative and size must be between 1 and 50.");
        }
        return PageRequest.of(page, size, sort);
    }

    public static PageRequest newest(int page, int size) {
        return of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    }
}
