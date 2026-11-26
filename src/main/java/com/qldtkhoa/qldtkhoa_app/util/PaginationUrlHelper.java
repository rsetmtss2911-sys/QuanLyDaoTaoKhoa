package com.qldtkhoa.qldtkhoa_app.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component("pagerUrl")
public class PaginationUrlHelper {

    public String page(String baseUrl, int pageNumber, int pageSize) {
        try {
            return ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", pageNumber)
                    .replaceQueryParam("size", pageSize)
                    .build()
                    .toUriString();
        } catch (IllegalStateException ex) {
            String prefix = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "";
            return prefix + "?page=" + pageNumber + "&size=" + pageSize;
        }
    }
}
