package com.qldtkhoa.qldtkhoa_app.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportUtilsTest {

    @Test
    @DisplayName("toCsv: Bắt đầu bằng UTF-8 BOM và thoát chuẩn RFC 4180 cho dấu phẩy, ngoặc kép, xuống dòng")
    void toCsv_includesBomAndEscapesRfc4180() {
        String csv = CsvExportUtils.toCsv(
                List.of("Mã SV", "Ghi chú"),
                List.of(List.of("SV01", "Học kỳ 1, điểm \"tốt\"\ndòng 2"))
        );

        assertThat(csv).startsWith("\uFEFF");
        assertThat(csv).contains("\"Học kỳ 1, điểm \"\"tốt\"\"\ndòng 2\"");
    }

    @Test
    @DisplayName("sanitizeAndEscape: Vô hiệu hóa công thức Excel (=, +, -, @, tab, CR và có khoảng trắng đầu)")
    void sanitizeAndEscape_neutralizesFormulaInjection() {
        assertThat(CsvExportUtils.sanitizeAndEscape("=SUM(A1:A2)")).isEqualTo("'=SUM(A1:A2)");
        assertThat(CsvExportUtils.sanitizeAndEscape("+cmd|' /C calc'!A0")).isEqualTo("'+cmd|' /C calc'!A0");
        assertThat(CsvExportUtils.sanitizeAndEscape("-1+1")).isEqualTo("'-1+1");
        assertThat(CsvExportUtils.sanitizeAndEscape("@SUM(1+1)")).isEqualTo("'@SUM(1+1)");
        assertThat(CsvExportUtils.sanitizeAndEscape("\t=1+1")).isEqualTo("'\t=1+1");
        assertThat(CsvExportUtils.sanitizeAndEscape("\r=1+1")).isEqualTo("\"'\r=1+1\"");
        assertThat(CsvExportUtils.sanitizeAndEscape("   =IMPORTXML()")).isEqualTo("'   =IMPORTXML()");
        assertThat(CsvExportUtils.sanitizeAndEscape(null)).isEmpty();
    }

    @Test
    @DisplayName("csvResponse: Trả về header Content-Type UTF-8, attachment và X-Content-Type-Options: nosniff")
    void csvResponse_setsSecurityAndDownloadHeaders() {
        ResponseEntity<byte[]> resp = CsvExportUtils.csvResponse(
                "bang diem/2026?.csv",
                List.of("A", "B"),
                List.of(List.of("1", "Nguyễn Văn A"))
        );

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getHeaders().getContentType().toString()).contains("text/csv").contains("UTF-8");
        assertThat(resp.getHeaders().getContentDisposition().getType()).isEqualTo("attachment");
        assertThat(resp.getHeaders().getContentDisposition().getFilename()).isEqualTo("bang_diem_2026_.csv");
        assertThat(resp.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        String body = new String(resp.getBody(), StandardCharsets.UTF_8);
        assertThat(body).startsWith("\uFEFF");
        assertThat(body).contains("Nguyễn Văn A");
    }
}
