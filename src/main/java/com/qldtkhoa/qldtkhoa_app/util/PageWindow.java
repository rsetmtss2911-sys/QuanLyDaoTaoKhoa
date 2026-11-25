package com.qldtkhoa.qldtkhoa_app.util;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.IntStream;

public record PageWindow(int current, int totalPages, int start, int end, List<Integer> pages) {

    public static PageWindow of(Page<?> page, int delta) {
        int total = page.getTotalPages();
        int current = page.getNumber();
        if (total <= 0) {
            return new PageWindow(0, 0, 0, 0, List.of());
        }
        int start = Math.max(0, current - delta);
        int end = Math.min(total - 1, current + delta);
        List<Integer> pageNumbers = IntStream.rangeClosed(start, end).boxed().toList();
        return new PageWindow(current, total, start, end, pageNumbers);
    }

    public static PageWindow of(Page<?> page) {
        return of(page, 2);
    }
}
