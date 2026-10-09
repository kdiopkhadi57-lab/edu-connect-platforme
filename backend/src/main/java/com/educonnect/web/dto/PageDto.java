package com.educonnect.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageDto<T>(List<T> items, int page, int totalPages, long totalItems, boolean last) {

    public static <E, T> PageDto<T> of(Page<E> page, Function<List<E>, List<T>> mapper) {
        return new PageDto<>(mapper.apply(page.getContent()), page.getNumber(), page.getTotalPages(),
                page.getTotalElements(), page.isLast());
    }
}
