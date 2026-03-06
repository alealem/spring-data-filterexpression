package com.example.demo.controllers.utils;

import org.springframework.data.domain.Page;
import java.util.function.Function;

public final class Pagination {
    private Pagination() {}

    public static <E, D> PaginatedResult<D> from(Page<E> page, Function<E, D> mapper) {
        return new PaginatedResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}