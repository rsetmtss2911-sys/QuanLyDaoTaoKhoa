package com.qldtkhoa.qldtkhoa_app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class SqlServerErrorsTest {

    @Test
    @DisplayName("translateToMessageKey: Ánh xạ đầy đủ mọi mã lỗi SQL Server v2 và v3")
    void translateToMessageKey_mapsAllCodes() {
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50010", "S0001", 50010))).isEqualTo("grade.error.withdrawn");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50011", "S0001", 50011))).isEqualTo("grade.error.examBanned");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50012", "S0001", 50012))).isEqualTo("monhoc.trongSoLocked");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50013", "S0001", 50013))).isEqualTo("grade.error.classCancelled");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50020", "S0001", 50020))).isEqualTo("schedule.error.roomConflict");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50021", "S0001", 50021))).isEqualTo("schedule.error.lecturerConflict");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50022", "S0001", 50022))).isEqualTo("schedule.error.selfOverlap");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50030", "S0001", 50030))).isEqualTo("reg.error.studentStatus");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50031", "S0001", 50031))).isEqualTo("reg.error.capacityFull");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50032", "S0001", 50032))).isEqualTo("reg.error.duplicateCourseInTerm");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50033", "S0001", 50033))).isEqualTo("reg.error.scheduleConflict");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50040", "S0001", 50040))).isEqualTo("decision.error.immutable");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50041", "S0001", 50041))).isEqualTo("audit.error.immutable");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50050", "S0001", 50050))).isEqualTo("course.error.prerequisiteCycle");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50060", "S0001", 50060))).isEqualTo("reg.error.sectionNotOpen");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50061", "S0001", 50061))).isEqualTo("reg.error.sectionNotOpen");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50062", "S0001", 50062))).isEqualTo("reg.alreadyRegistered");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50063", "S0001", 50063))).isEqualTo("reg.error.examBannedReRegister");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50064", "S0001", 50064))).isEqualTo("sinhvien.notFound");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("2627", "23000", 2627))).isEqualTo("data.error.duplicate");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("2601", "23000", 2601))).isEqualTo("data.error.duplicate");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("547", "23000", 547))).isEqualTo("data.error.constraint");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("515", "23000", 515))).isEqualTo("data.error.nullValue");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("2628", "22001", 2628))).isEqualTo("data.error.tooLong");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("8152", "22001", 8152))).isEqualTo("data.error.tooLong");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("1205", "40001", 1205))).isEqualTo("data.error.concurrency");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("1222", "HY000", 1222))).isEqualTo("data.error.concurrency");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("99999", "HY000", 99999))).isEqualTo("data.error.generic");
        assertThat(SqlServerErrors.translateToMessageKey(new RuntimeException("non-sql"))).isEqualTo("data.error.generic");
    }

    @Test
    @DisplayName("extractErrorCode & describe: Duyệt BatchUpdateException getNextException() và xử lý null")
    void extractErrorCode_batchUpdateExceptionNextException_returnsRealCode() {
        BatchUpdateException batchEx = new BatchUpdateException("Batch failed", "S1000", 0, new int[]{-3});
        batchEx.setNextException(new SQLException("Trigger 50012", "S0001", 50012));

        RuntimeException wrapper = new RuntimeException("JPA failure", batchEx);

        assertThat(SqlServerErrors.extractErrorCode(wrapper)).isEqualTo(50012);
        assertThat(SqlServerErrors.translateToMessageKey(wrapper)).isEqualTo("monhoc.trongSoLocked");
        assertThat(SqlServerErrors.describe(wrapper)).isEqualTo("sqlserver-error-code=50012");
        assertThat(SqlServerErrors.extractErrorCode(null)).isEqualTo(-1);
    }
}
