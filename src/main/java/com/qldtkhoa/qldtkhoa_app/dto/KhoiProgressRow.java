package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;

public record KhoiProgressRow(
        KhoiKienThuc khoiKienThuc,
        int requiredMandatoryCredits,
        int earnedMandatoryCredits,
        int minElectiveCredits,
        int earnedElectiveCredits,
        int totalRequired,
        int totalEarned,
        int progressPercent
) {
}
