package com.qldtkhoa.qldtkhoa_app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class LikeUtilsAndSortWhitelistTest {

    @Test
    @DisplayName("LikeUtils.escape: Escape an toàn %, _, \\, [ cho SQL Server và H2")
    void escape_specialWildcards_escapesProperly() {
        assertThat(LikeUtils.escape(null)).isEmpty();
        assertThat(LikeUtils.escape("   ")).isEmpty();
        assertThat(LikeUtils.escape("  100%_test\\path[a]  "))
                .isEqualTo("100\\%\\_test\\\\path\\[a]");
    }

    @Test
    @DisplayName("SortWhitelist: Luôn trả về ORDER BY xác định khi client truyền trường lạ hoặc unsorted")
    void sortWhitelist_unknownOrUnsorted_returnsDeterministicFallbackSort() {
        Pageable unsorted = PageRequest.of(0, 20);
        Pageable resultUnsorted = SortWhitelist.of(unsorted, "maSV", "hoTen");
        assertThat(resultUnsorted.getSort().isSorted()).isTrue();
        assertThat(resultUnsorted.getSort().getOrderFor("maSV")).isNotNull();

        Pageable invalidSort = PageRequest.of(0, 20, Sort.by("nonExistentColumn"));
        Pageable resultInvalid = SortWhitelist.of(invalidSort, "maSV", "hoTen");
        assertThat(resultInvalid.getSort().isSorted()).isTrue();
        assertThat(resultInvalid.getSort().getOrderFor("maSV")).isNotNull();

        Pageable validSort = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("hoTen")));
        Pageable resultValid = SortWhitelist.of(validSort, "maSV", "hoTen");
        assertThat(resultValid.getSort().getOrderFor("hoTen")).isNotNull();
        assertThat(resultValid.getSort().getOrderFor("hoTen").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
