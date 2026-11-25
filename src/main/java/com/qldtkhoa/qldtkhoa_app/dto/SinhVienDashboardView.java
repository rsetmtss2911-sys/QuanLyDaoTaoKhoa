package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;

import java.util.List;

public record SinhVienDashboardView(
        String maSV,
        String hoTen,
        String email,
        String maLopHC,
        String tenLopHC,
        String tenNganh,
        String tenKhoaHoc,
        TrangThaiSinhVien trangThaiHienTai,
        String tenCoVan,
        String emailCoVan,
        String currentHk,
        String currentHkName,
        List<HocKyNamHoc> hocKys,
        int registeredCreditsCurrentTerm,
        int minCreditsTerm,
        int maxCreditsTerm,
        int totalCumulativeCredits,
        Double cumulativeGpa4,
        String academicStanding,
        List<DangKyDaChonRow> currentRegistrations,
        List<TimetableSlot> weeklySchedule,
        List<QuyetDinhRow> recentDecisions
) {
}
