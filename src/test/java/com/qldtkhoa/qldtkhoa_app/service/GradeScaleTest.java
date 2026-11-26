package com.qldtkhoa.qldtkhoa_app.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradeScaleTest {

    @Test
    @DisplayName("calculateTotal: Làm tròn HALF_UP 1 chữ số thập phân và hỗ trợ trọng số 0.0")
    void calculateTotal_halfUpRoundingAndZeroWeight_behavesCorrectly() {
        assertThat(GradeScale.calculateTotal(8.25, 8.25, 8.25, 0.2, 0.2, 0.6)).isEqualTo(8.3);
        assertThat(GradeScale.calculateTotal(null, 8.0, 9.0, 0.0, 0.4, 0.6)).isEqualTo(8.6);
        assertThat(GradeScale.calculateTotal(7.0, null, 8.0, 0.2, 0.3, 0.5)).isNull();
    }

    @Test
    @DisplayName("toLetter & toGpa4: Kiểm tra đầy đủ các mốc ranh giới thang 10 -> chữ -> hệ 4")
    void toLetterAndGpa4_boundaryValues_matchRegulation() {
        assertThat(GradeScale.toLetter(8.5)).isEqualTo("A");
        assertThat(GradeScale.toGpa4(8.5)).isEqualTo(4.0);

        assertThat(GradeScale.toLetter(8.0)).isEqualTo("B+");
        assertThat(GradeScale.toGpa4(8.0)).isEqualTo(3.5);

        assertThat(GradeScale.toLetter(7.0)).isEqualTo("B");
        assertThat(GradeScale.toGpa4(7.0)).isEqualTo(3.0);

        assertThat(GradeScale.toLetter(6.5)).isEqualTo("C+");
        assertThat(GradeScale.toGpa4(6.5)).isEqualTo(2.5);

        assertThat(GradeScale.toLetter(5.5)).isEqualTo("C");
        assertThat(GradeScale.toGpa4(5.5)).isEqualTo(2.0);

        assertThat(GradeScale.toLetter(5.0)).isEqualTo("D+");
        assertThat(GradeScale.toGpa4(5.0)).isEqualTo(1.5);

        assertThat(GradeScale.toLetter(4.0)).isEqualTo("D");
        assertThat(GradeScale.toGpa4(4.0)).isEqualTo(1.0);

        assertThat(GradeScale.toLetter(3.9)).isEqualTo("F");
        assertThat(GradeScale.toGpa4(3.9)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("classifyStanding: Xếp loại học lực bao gồm mức Kém (< 1.0)")
    void classifyStanding_allBandsIncludingKem_returnsExpectedLabel() {
        assertThat(GradeScale.classifyStanding(null)).isEqualTo("—");
        assertThat(GradeScale.classifyStanding(3.6)).isEqualTo("Xuất sắc");
        assertThat(GradeScale.classifyStanding(3.2)).isEqualTo("Giỏi");
        assertThat(GradeScale.classifyStanding(2.5)).isEqualTo("Khá");
        assertThat(GradeScale.classifyStanding(2.0)).isEqualTo("Trung bình");
        assertThat(GradeScale.classifyStanding(1.0)).isEqualTo("Yếu");
        assertThat(GradeScale.classifyStanding(0.99)).isEqualTo("Kém");
    }
}
