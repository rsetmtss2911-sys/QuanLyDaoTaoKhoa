package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.entity.view.ThongKeLopHocPhanView;

import java.util.List;

public record AdminDashboardView(
        long totalStudentsActive,
        long totalStudentsTotal,
        long totalLecturers,
        long totalSectionsCurrentTerm,
        long totalOpenSections,
        long totalCourses,
        long totalAdministrativeClasses,
        String currentHk,
        String currentHkName,
        List<HocKyNamHoc> hocKys,
        List<LopHocPhanRow> pendingSections,
        List<QuyetDinhRow> recentDecisions,
        List<ThongKeLopHocPhanView> crowdedSections
) {
}
