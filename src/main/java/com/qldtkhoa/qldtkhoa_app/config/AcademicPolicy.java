package com.qldtkhoa.qldtkhoa_app.config;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GpaRepeatPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * Tham số quy chế học vụ đọc từ tiền tố {@code app.academic} trong {@code application.properties}.
 */
@ConfigurationProperties(prefix = "app.academic")
public record AcademicPolicy(
        double passScore,
        int minCreditsPerTerm,
        int maxCreditsPerTerm,
        int registrationWindowDays,
        int withdrawDeadlineDays,
        GpaRepeatPolicy gpaRepeatPolicy,
        int minSectionStudents,
        String decisionSyncCron,
        String timezone
) {
    @ConstructorBinding
    public AcademicPolicy {
        if (gpaRepeatPolicy == null) {
            gpaRepeatPolicy = GpaRepeatPolicy.HIGHEST;
        }
        if (minSectionStudents <= 0) {
            minSectionStudents = 5;
        }
        if (decisionSyncCron == null || decisionSyncCron.isBlank()) {
            decisionSyncCron = "0 5 0 * * *";
        }
        if (timezone == null || timezone.isBlank()) {
            timezone = "Asia/Ho_Chi_Minh";
        }
    }

    public AcademicPolicy(
            double passScore,
            int minCreditsPerTerm,
            int maxCreditsPerTerm,
            int registrationWindowDays,
            int withdrawDeadlineDays
    ) {
        this(passScore, minCreditsPerTerm, maxCreditsPerTerm, registrationWindowDays, withdrawDeadlineDays,
                GpaRepeatPolicy.HIGHEST, 5, "0 5 0 * * *", "Asia/Ho_Chi_Minh");
    }
}
