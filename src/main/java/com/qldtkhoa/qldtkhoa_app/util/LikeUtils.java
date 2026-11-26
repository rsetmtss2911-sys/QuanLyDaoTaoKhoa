package com.qldtkhoa.qldtkhoa_app.util;

/**
 * Tiện ích escape ký tự wildcard trong câu lệnh JPQL {@code LIKE ... ESCAPE '\'}
 * để tương thích an toàn với cả Microsoft SQL Server ({@code %}, {@code _}, {@code [}) và H2.
 */
public final class LikeUtils {

    private LikeUtils() {
    }

    public static String escape(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.trim();
        StringBuilder sb = new StringBuilder(trimmed.length() + 4);
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '\\' || c == '%' || c == '_' || c == '[') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
