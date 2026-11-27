package com.qldtkhoa.qldtkhoa_app.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Tiện ích xuất dữ liệu CSV an toàn:
 * - Gắn UTF-8 BOM để Microsoft Excel mở hiển thị đúng tiếng Việt có dấu.
 * - Chuẩn RFC 4180 (bao ngoặc kép khi chứa dấu phẩy, ngoặc kép hoặc xuống dòng).
 * - Chống tấn công CSV Formula Injection (thêm dấu nháy đơn {@code '} trước ký tự công thức).
 */
public final class CsvExportUtils {

    private static final String UTF8_BOM = "\uFEFF";

    private CsvExportUtils() {
    }

    public static String toCsv(List<String> headers, List<? extends List<String>> rows) {
        StringBuilder sb = new StringBuilder(UTF8_BOM);
        appendRow(sb, headers);
        if (rows != null) {
            for (List<String> row : rows) {
                appendRow(sb, row);
            }
        }
        return sb.toString();
    }

    public static ResponseEntity<byte[]> csvResponse(String fileName, List<String> headers, List<? extends List<String>> rows) {
        byte[] bytes = toCsv(headers, rows).getBytes(StandardCharsets.UTF_8);
        String safeFileName = (fileName == null || fileName.isBlank()) ? "export.csv" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        httpHeaders.setContentDisposition(ContentDisposition.attachment().filename(safeFileName, StandardCharsets.UTF_8).build());
        httpHeaders.set("X-Content-Type-Options", "nosniff");
        return ResponseEntity.ok().headers(httpHeaders).body(bytes);
    }

    static String sanitizeAndEscape(String raw) {
        if (raw == null) {
            return "";
        }
        String value = neutralizeFormula(raw);
        boolean mustQuote = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (!mustQuote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String neutralizeFormula(String raw) {
        if (raw.isEmpty()) {
            return raw;
        }
        char first = raw.charAt(0);
        if (isFormulaTrigger(first)) {
            return "'" + raw;
        }
        String trimmed = raw.stripLeading();
        if (!trimmed.isEmpty() && isFormulaTrigger(trimmed.charAt(0))) {
            return "'" + raw;
        }
        return raw;
    }

    private static boolean isFormulaTrigger(char c) {
        return c == '=' || c == '+' || c == '-' || c == '@' || c == '\t' || c == '\r';
    }

    private static void appendRow(StringBuilder sb, List<String> cells) {
        if (cells == null || cells.isEmpty()) {
            sb.append("\r\n");
            return;
        }
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(sanitizeAndEscape(cells.get(i)));
        }
        sb.append("\r\n");
    }
}
