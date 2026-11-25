package com.qldtkhoa.qldtkhoa_app.dto;

import java.util.List;
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
        Set<String> passedSubjects
) {
}
