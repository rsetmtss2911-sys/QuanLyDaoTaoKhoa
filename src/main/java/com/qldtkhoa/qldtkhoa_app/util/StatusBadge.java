package com.qldtkhoa.qldtkhoa_app.util;

import org.springframework.stereotype.Component;

@Component("statusBadge")
public class StatusBadge {

    public String variant(Object obj) {
        if (obj == null) return "secondary";
        if (obj instanceof com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled labeled) {
            return variant(labeled.getLabel());
        }
        return variant(obj.toString());
    }

    public String icon(Object obj) {
        if (obj == null) return "bi-circle";
        if (obj instanceof com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled labeled) {
            return icon(labeled.getLabel());
        }
        return icon(obj.toString());
    }

    public String label(Object obj) {
        if (obj == null) return "—";
        if (obj instanceof com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled labeled) {
            return labeled.getLabel();
        }
        return obj.toString();
    }

    public String variant(String label) {
        if (label == null || label.isBlank()) {
            return "secondary";
        }
        return switch (label.trim()) {
            case "Hợp lệ", "Đang học", "Đã chốt danh sách", "Đạt" -> "success";
            case "Đã khóa điểm" -> "primary";
            case "Chờ mở", "Bảo lưu", "Chưa có điểm" -> "warning";
            case "Cấm thi", "Đình chỉ", "Thôi học", "Đã hủy", "Học lại" -> "danger";
            case "Đã rút lớp", "Trở lại học", "Đã tốt nghiệp", "Tốt nghiệp" -> "info";
            default -> "secondary";
        };
    }

    public String icon(String label) {
        if (label == null || label.isBlank()) {
            return "bi-circle";
        }
        return switch (label.trim()) {
            case "Hợp lệ", "Đang học", "Đạt" -> "bi-check-circle-fill";
            case "Đã chốt danh sách" -> "bi-lock-fill";
            case "Đã khóa điểm" -> "bi-patch-check-fill";
            case "Chờ mở" -> "bi-hourglass-split";
            case "Bảo lưu" -> "bi-pause-circle-fill";
            case "Cấm thi", "Đình chỉ" -> "bi-exclamation-triangle-fill";
            case "Thôi học", "Đã hủy", "Học lại" -> "bi-x-circle-fill";
            case "Đã rút lớp" -> "bi-arrow-return-left";
            case "Trở lại học" -> "bi-arrow-repeat";
            case "Đã tốt nghiệp", "Tốt nghiệp" -> "bi-mortarboard-fill";
            default -> "bi-info-circle";
        };
    }
}
