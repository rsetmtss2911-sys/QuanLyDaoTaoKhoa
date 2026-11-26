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
        Sort defaultSort = (allowedProperties != null && allowedProperties.length > 0)
                ? Sort.by(allowedProperties[0]).ascending()
                : Sort.unsorted();
        return of(pageable, defaultSort, allowedProperties);
    }

    public static Pageable of(Pageable pageable, Sort defaultSort, String... allowedProperties) {
        Sort fallback = defaultSort != null ? defaultSort : Sort.unsorted();
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), fallback);
        }
        Set<String> allowedSet = allowedProperties != null
                ? Arrays.stream(allowedProperties).collect(Collectors.toSet())
                : Set.of();

        List<Sort.Order> safeOrders = pageable.getSort().stream()
                .filter(order -> allowedSet.contains(order.getProperty()))
                .toList();

        if (safeOrders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), fallback);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }
}
