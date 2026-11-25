package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;

import java.time.LocalDateTime;

public record DangKyDaChonRow(
        String maLHP,
        String maMon,
        String tenMon,
        Integer soTinChi,
        String tenGV,
        String lichHoc,
        LocalDateTime ngayDangKy,
        TrangThaiDangKy trangThaiDangKy,
        TrangThaiLop trangThaiLop
) {
    public DangKyDaChonRow(
            String maLHP,
            String maMon,
            String tenMon,
            Integer soTinChi,
            String tenGV,
            String lichHoc,
            LocalDateTime ngayDangKy,
            TrangThaiDangKy trangThaiDangKy
    ) {
        this(maLHP, maMon, tenMon, soTinChi, tenGV, lichHoc, ngayDangKy, trangThaiDangKy, TrangThaiLop.CHO_MO);
    }

    public String tenGiangVien() {
        return tenGV;
    }

    public boolean coTheRut() {
        return trangThaiDangKy == TrangThaiDangKy.HOP_LE && trangThaiLop == TrangThaiLop.CHO_MO;
    }
}
