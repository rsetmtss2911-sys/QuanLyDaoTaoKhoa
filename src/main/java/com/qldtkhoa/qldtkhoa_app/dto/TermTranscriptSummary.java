package com.qldtkhoa.qldtkhoa_app.dto;

public record TermTranscriptSummary(
        String termLabel,
        int gradedCredits,
        int passedCredits,
        Double gpa4
) {
}
