package com.qldtkhoa.qldtkhoa_app.dto;

import java.time.LocalDateTime;

public record NhatKyRow(
        Long maNhatKy,
        LocalDateTime thoiGian,
        String nguoiThucHien,
        String hanhDong,
        String doiTuong,
        String maBanGhi,
        String giaTriCu,
        String giaTriMoi,
        String lyDo
) {
}
