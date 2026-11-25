package com.qldtkhoa.qldtkhoa_app.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SortWhitelist {

    private SortWhitelist() {}

    public static Pageable of(Pageable pageable, String... allowedProperties) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }
        Set<String> allowedSet = Arrays.stream(allowedProperties)
                .collect(Collectors.toSet());

        List<Sort.Order> safeOrders = pageable.getSort().stream()
                .filter(order -> allowedSet.contains(order.getProperty()))
                .toList();

        if (safeOrders.isEmpty()) {
            // Nếu không có thuộc tính nào hợp lệ, fallback về unsorted
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }
}
