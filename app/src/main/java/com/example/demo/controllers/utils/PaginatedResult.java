package com.example.demo.controllers.utils;

import java.util.List;

public record PaginatedResult<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages,
    boolean first,
    boolean last) {}
