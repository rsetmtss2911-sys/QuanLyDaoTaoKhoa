package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;

import java.time.LocalDate;
import java.util.List;

public record CoVanStudentRow(
        String maSV,
        String hoTen,
        GioiTinh gioiTinh,
        LocalDate ngaySinh,
        String email,
        TrangThaiSinhVien trangThaiHienTai,
        Double cpa4,
        String xepLoai,
        int tongTCTichLuy,
        int soMonTruot,
        boolean coCamThi,
        boolean canhBaoHocVu,
        List<String> lyDoCanhBao
) {
    public boolean isAtRisk() {
        return canhBaoHocVu || coCamThi || soMonTruot > 0;
    }
}
