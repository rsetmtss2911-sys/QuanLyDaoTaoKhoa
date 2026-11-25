package com.qldtkhoa.qldtkhoa_app.util;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusBadgeTest {

    private final StatusBadge statusBadge = new StatusBadge();

    @Test
    @DisplayName("variant and icon should handle TrangThaiSinhVien enum correctly")
    void testTrangThaiSinhVienVariants() {
        assertThat(statusBadge.variant(TrangThaiSinhVien.DANG_HOC)).isEqualTo("success");
        assertThat(statusBadge.icon(TrangThaiSinhVien.DANG_HOC)).isEqualTo("bi-check-circle-fill");

        assertThat(statusBadge.variant(TrangThaiSinhVien.BAO_LUU)).isEqualTo("warning");
        assertThat(statusBadge.icon(TrangThaiSinhVien.BAO_LUU)).isEqualTo("bi-pause-circle-fill");

        assertThat(statusBadge.variant(TrangThaiSinhVien.THOI_HOC)).isEqualTo("danger");
        assertThat(statusBadge.icon(TrangThaiSinhVien.THOI_HOC)).isEqualTo("bi-x-circle-fill");
    }

    @Test
    @DisplayName("variant and icon should handle TrangThaiLop, TrangThaiDangKy, and LoaiQuyetDinh correctly")
    void testOtherEnumVariants() {
        assertThat(statusBadge.variant(TrangThaiLop.CHO_MO)).isEqualTo("warning");
        assertThat(statusBadge.variant(TrangThaiDangKy.HOP_LE)).isEqualTo("success");
        assertThat(statusBadge.variant(TrangThaiDangKy.DA_RUT_LOP)).isEqualTo("info");
        assertThat(statusBadge.variant(com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh.BAO_LUU)).isEqualTo("warning");
        assertThat(statusBadge.variant(com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh.DINH_CHI)).isEqualTo("danger");
    }

    @Test
    @DisplayName("variant and icon should return defaults for null or unknown")
    void testNullAndUnknown() {
        assertThat(statusBadge.variant((Object) null)).isEqualTo("secondary");
        assertThat(statusBadge.icon((Object) null)).isEqualTo("bi-circle");
        assertThat(statusBadge.variant("")).isEqualTo("secondary");
    }
}
