package com.qldtkhoa.qldtkhoa_app.util;

import java.sql.SQLException;

public final class SqlServerErrors {

    private SqlServerErrors() {}

    public static int extractErrorCode(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof SQLException sqlException) {
                return sqlException.getErrorCode();
            }
        }
        return -1;
    }

    public static String translateToMessageKey(Throwable throwable) {
        int code = extractErrorCode(throwable);
        return switch (code) {
            case 50010 -> "grade.error.withdrawn";
            case 50011 -> "grade.error.examBanned";
            case 50020 -> "schedule.error.roomConflict";
            case 50021 -> "schedule.error.lecturerConflict";
            case 50030 -> "reg.error.studentStatus";
            case 50031 -> "reg.error.capacityFull";
            case 50032 -> "reg.error.duplicateCourseInTerm";
            case 50033 -> "reg.error.scheduleConflict";
            case 50040 -> "decision.error.immutable";
            case 50050 -> "course.error.prerequisiteCycle";
            case 50060, 50061 -> "reg.error.sectionNotOpen";
            case 2627, 2601 -> "data.error.duplicate";
            case 547 -> "data.error.constraint";
            case 1205 -> "data.error.concurrency";
            default -> "data.error.generic";
        };
    }

    public static String describe(Throwable throwable) {
        return "sqlserver-error-code=" + extractErrorCode(throwable);
    }
}
