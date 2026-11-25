package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;

import java.util.List;

public record GiangVienDashboardView(
        String maNS,
        String hoTen,
        String hocVi,
        String tenBoMon,
        String currentHk,
        String currentHkName,
        List<HocKyNamHoc> hocKys,
        long totalAssignedSections,
        long totalStudentsTeaching,
        long totalWeeklyTeachingHours,
        long totalAdvisedClasses,
        List<LopHocPhanRow> currentSections,
        List<TimetableSlot> weeklySchedule,
        List<LopHanhChinhRow> advisedClasses
) {
}
