package com.qldtkhoa.qldtkhoa_app.dto;

import java.util.List;

public record CoVanClassRosterView(
        LopHanhChinhDetailView lop,
        List<SinhVienRow> students,
        List<CoVanStudentRow> studentRows,
        long atRiskCount,
        int totalStudents
) {
}
