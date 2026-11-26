package com.qldtkhoa.qldtkhoa_app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class SqlServerErrorsTest {

    @Test
    @DisplayName("translateToMessageKey: Ánh xạ đầy đủ mã lỗi SQL Server v2 và v3")
    void translateToMessageKey_mapsV2AndV3Codes() {
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50011", "S0001", 50011))).isEqualTo("grade.error.examBanned");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50012", "S0001", 50012))).isEqualTo("monhoc.trongSoLocked");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50013", "S0001", 50013))).isEqualTo("grade.error.classCancelled");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50022", "S0001", 50022))).isEqualTo("schedule.error.selfOverlap");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50041", "S0001", 50041))).isEqualTo("audit.error.immutable");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50062", "S0001", 50062))).isEqualTo("reg.alreadyRegistered");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50063", "S0001", 50063))).isEqualTo("reg.error.examBannedReRegister");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("50064", "S0001", 50064))).isEqualTo("sinhvien.notFound");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("515", "23000", 515))).isEqualTo("data.error.nullValue");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("2628", "22001", 2628))).isEqualTo("data.error.tooLong");
        assertThat(SqlServerErrors.translateToMessageKey(new SQLException("1222", "HY000", 1222))).isEqualTo("data.error.concurrency");
    }

    @Test
    @DisplayName("extractErrorCode: Duyệt qua BatchUpdateException có errorCode=0 để lấy mã lỗi ở getNextException()")
    void extractErrorCode_batchUpdateExceptionNextException_returnsRealCode() {
        BatchUpdateException batchEx = new BatchUpdateException("Batch failed", "S1000", 0, new int[]{-3});
        batchEx.setNextException(new SQLException("Trigger 50012", "S0001", 50012));

        RuntimeException wrapper = new RuntimeException("JPA failure", batchEx);

        assertThat(SqlServerErrors.extractErrorCode(wrapper)).isEqualTo(50012);
        assertThat(SqlServerErrors.translateToMessageKey(wrapper)).isEqualTo("monhoc.trongSoLocked");
    }
}
