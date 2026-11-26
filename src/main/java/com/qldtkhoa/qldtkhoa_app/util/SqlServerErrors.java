package com.qldtkhoa.qldtkhoa_app.util;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public final class SqlServerErrors {

    private SqlServerErrors() {}

    public static int extractErrorCode(Throwable throwable) {
        Set<Throwable> visited = new HashSet<>();
        for (Throwable current = throwable; current != null && visited.add(current); current = current.getCause()) {
            if (current instanceof SQLException sqlException) {
                for (SQLException next = sqlException; next != null; next = next.getNextException()) {
                    if (next.getErrorCode() != 0) {
                        return next.getErrorCode();
                    }
                    if (!visited.add(next) && next != sqlException) {
                        break;
                    }
                }
            }
        }
        return -1;
    }

    public static String translateToMessageKey(Throwable throwable) {
        int code = extractErrorCode(throwable);
        return switch (code) {
            case 50010 -> "grade.error.withdrawn";
            case 50011 -> "grade.error.examBanned";
            case 50012 -> "monhoc.trongSoLocked";
            case 50013 -> "grade.error.classCancelled";
            case 50020 -> "schedule.error.roomConflict";
            case 50021 -> "schedule.error.lecturerConflict";
            case 50022 -> "schedule.error.selfOverlap";
            case 50030 -> "reg.error.studentStatus";
            case 50031 -> "reg.error.capacityFull";
            case 50032 -> "reg.error.duplicateCourseInTerm";
            case 50033 -> "reg.error.scheduleConflict";
            case 50040 -> "decision.error.immutable";
            case 50041 -> "audit.error.immutable";
            case 50050 -> "course.error.prerequisiteCycle";
            case 50060, 50061 -> "reg.error.sectionNotOpen";
            case 50062 -> "reg.alreadyRegistered";
            case 50063 -> "reg.error.examBannedReRegister";
            case 50064 -> "sinhvien.notFound";
            case 2627, 2601 -> "data.error.duplicate";
            case 547 -> "data.error.constraint";
            case 515 -> "data.error.nullValue";
            case 2628, 8152 -> "data.error.tooLong";
            case 1205, 1222 -> "data.error.concurrency";
            default -> "data.error.generic";
        };
    }

    public static String describe(Throwable throwable) {
        return "sqlserver-error-code=" + extractErrorCode(throwable);
    }
}
