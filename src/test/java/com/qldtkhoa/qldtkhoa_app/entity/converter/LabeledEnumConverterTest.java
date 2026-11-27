package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LabeledEnumConverterTest {

    private <E extends Enum<E> & DbLabeled> void assertConverterRoundTrip(
            LabeledEnumConverter<E> converter,
            E[] values) {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();

        for (E val : values) {
            String dbValue = converter.convertToDatabaseColumn(val);
            assertThat(dbValue).isEqualTo(val.getLabel());
            assertThat(converter.convertToEntityAttribute(dbValue)).isEqualTo(val);
            assertThat(converter.convertToEntityAttribute(" " + dbValue + " ")).isEqualTo(val);
        }

        assertThatThrownBy(() -> converter.convertToEntityAttribute("KHONG_TON_TAI"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Tất cả 7 LabeledEnumConverter chuyển đổi 2 chiều chính xác với nhãn tiếng Việt và null/lỗi")
    void allSevenConverters_roundTripAndInvalidInput() {
        List<Runnable> checks = List.of(
                () -> assertConverterRoundTrip(new GioiTinhConverter(), GioiTinh.values()),
                () -> assertConverterRoundTrip(new KhoiKienThucConverter(), KhoiKienThuc.values()),
                () -> assertConverterRoundTrip(new LoaiHocPhanConverter(), LoaiHocPhan.values()),
                () -> assertConverterRoundTrip(new LoaiQuyetDinhConverter(), LoaiQuyetDinh.values()),
                () -> assertConverterRoundTrip(new TrangThaiDangKyConverter(), TrangThaiDangKy.values()),
                () -> assertConverterRoundTrip(new TrangThaiLopConverter(), TrangThaiLop.values()),
                () -> assertConverterRoundTrip(new TrangThaiSinhVienConverter(), TrangThaiSinhVien.values())
        );
        checks.forEach(check -> check.run());
    }
}
