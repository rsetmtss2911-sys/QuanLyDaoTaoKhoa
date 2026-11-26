package com.qldtkhoa.qldtkhoa_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chuẩn hóa công thức quy đổi điểm số (thang 10, thang 4, điểm chữ và xếp loại học lực)
 * dùng chung cho {@code DiemService}, {@code HocLucService} và các màn hình thống kê.
 */
public final class GradeScale {

    private GradeScale() {
    }

    /**
     * Tính điểm tổng kết thang 10 làm tròn 1 chữ số thập phân (HALF_UP),
     * khớp với {@code TRG_KetQuaHocTap_CalcDiemTong}.
     * Thành phần có trọng số {@code <= 0.0} không bắt buộc phải khác {@code null}.
     */
    public static Double calculateTotal(Double diemQT,
                                        Double diemGK,
                                        Double diemCK,
                                        Double wQt,
                                        Double wGk,
                                        Double wCk) {
        double weightQt = wQt != null ? wQt : 0.0;
        double weightGk = wGk != null ? wGk : 0.0;
        double weightCk = wCk != null ? wCk : 0.0;

        if ((weightQt > 0.0 && diemQT == null)
                || (weightGk > 0.0 && diemGK == null)
                || (weightCk > 0.0 && diemCK == null)) {
            return null;
        }

        BigDecimal qt = BigDecimal.valueOf(diemQT != null ? diemQT : 0.0);
        BigDecimal gk = BigDecimal.valueOf(diemGK != null ? diemGK : 0.0);
        BigDecimal ck = BigDecimal.valueOf(diemCK != null ? diemCK : 0.0);

        BigDecimal raw = qt.multiply(BigDecimal.valueOf(weightQt))
                .add(gk.multiply(BigDecimal.valueOf(weightGk)))
                .add(ck.multiply(BigDecimal.valueOf(weightCk)));

        return raw.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public static String toLetter(Double diemTong) {
        if (diemTong == null) {
            return "—";
        }
        if (diemTong >= 8.5) return "A";
        if (diemTong >= 8.0) return "B+";
        if (diemTong >= 7.0) return "B";
        if (diemTong >= 6.5) return "C+";
        if (diemTong >= 5.5) return "C";
        if (diemTong >= 5.0) return "D+";
        if (diemTong >= 4.0) return "D";
        return "F";
    }

    public static Double toGpa4(Double diemTong) {
        if (diemTong == null) {
            return null;
        }
        if (diemTong >= 8.5) return 4.0;
        if (diemTong >= 8.0) return 3.5;
        if (diemTong >= 7.0) return 3.0;
        if (diemTong >= 6.5) return 2.5;
        if (diemTong >= 5.5) return 2.0;
        if (diemTong >= 5.0) return 1.5;
        if (diemTong >= 4.0) return 1.0;
        return 0.0;
    }

    public static String classifyStanding(Double gpa4) {
        if (gpa4 == null) {
            return "—";
        }
        if (gpa4 >= 3.6) return "Xuất sắc";
        if (gpa4 >= 3.2) return "Giỏi";
        if (gpa4 >= 2.5) return "Khá";
        if (gpa4 >= 2.0) return "Trung bình";
        if (gpa4 >= 1.0) return "Yếu";
        return "Kém";
    }
}
