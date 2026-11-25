package com.qldtkhoa.qldtkhoa_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tham số quy chế học vụ đọc từ tiền tố {@code app.academic} trong {@code application.properties}.
 */
@ConfigurationProperties(prefix = "app.academic")
public record AcademicPolicy(
        double passScore,
        int minCreditsPerTerm,
        int maxCreditsPerTerm,
        int registrationWindowDays,
        int withdrawDeadlineDays
) {
}
