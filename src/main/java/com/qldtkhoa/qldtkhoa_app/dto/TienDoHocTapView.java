package com.qldtkhoa.qldtkhoa_app.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record TienDoHocTapView(
        String maSV,
        String hoTen,
        String tenNganh,
        String maKhoaHoc,
        int totalCurriculumCredits,
        int completedCredits,
        int progressPercent,
        List<CTDTRow> curriculum,
        Set<String> passedSubjects,
        List<KhoiProgressRow> blockProgress,
        Map<String, String> courseStatuses
) {
    public TienDoHocTapView(
            String maSV,
            String hoTen,
            String tenNganh,
            String maKhoaHoc,
            int totalCurriculumCredits,
            int completedCredits,
            int progressPercent,
            List<CTDTRow> curriculum,
            Set<String> passedSubjects
    ) {
        this(maSV, hoTen, tenNganh, maKhoaHoc, totalCurriculumCredits, completedCredits,
                progressPercent, curriculum, passedSubjects, List.of(), Map.of());
    }
}
