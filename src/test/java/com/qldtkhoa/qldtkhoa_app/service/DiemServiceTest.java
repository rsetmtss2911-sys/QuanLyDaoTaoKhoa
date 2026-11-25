package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KetQuaHocTapRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiemServiceTest {

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Mock
    private KetQuaHocTapRepository ketQuaHocTapRepository;

    @Mock
    private BangDiemSinhVienViewRepository bangDiemViewRepository;

    @Mock
    private com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy academicPolicy;

    @InjectMocks
    private DiemService diemService;

    @Test
    @DisplayName("calculateLetter: Thang điểm 10 chuyển đổi chuẩn xác sang Thang chữ A, B+, B, C+, C, D+, D, F")
    void calculateLetter_validScores_returnsCorrectLetterGrade() {
        assertThat(DiemService.calculateLetter(9.0)).isEqualTo("A");
        assertThat(DiemService.calculateLetter(8.5)).isEqualTo("A");
        assertThat(DiemService.calculateLetter(8.2)).isEqualTo("B+");
        assertThat(DiemService.calculateLetter(7.5)).isEqualTo("B");
        assertThat(DiemService.calculateLetter(6.8)).isEqualTo("C+");
        assertThat(DiemService.calculateLetter(5.8)).isEqualTo("C");
        assertThat(DiemService.calculateLetter(5.2)).isEqualTo("D+");
        assertThat(DiemService.calculateLetter(4.5)).isEqualTo("D");
        assertThat(DiemService.calculateLetter(3.9)).isEqualTo("F");
        assertThat(DiemService.calculateLetter(null)).isEqualTo("—");
    }

    @Test
    @DisplayName("calculateGpa4: Thang điểm 10 chuyển đổi chuẩn xác sang Thang hệ 4")
    void calculateGpa4_validScores_returnsCorrectGpa() {
        assertThat(DiemService.calculateGpa4(8.8)).isEqualTo(4.0);
        assertThat(DiemService.calculateGpa4(8.1)).isEqualTo(3.5);
        assertThat(DiemService.calculateGpa4(7.2)).isEqualTo(3.0);
        assertThat(DiemService.calculateGpa4(6.6)).isEqualTo(2.5);
        assertThat(DiemService.calculateGpa4(5.6)).isEqualTo(2.0);
        assertThat(DiemService.calculateGpa4(5.1)).isEqualTo(1.5);
        assertThat(DiemService.calculateGpa4(4.0)).isEqualTo(1.0);
        assertThat(DiemService.calculateGpa4(3.5)).isEqualTo(0.0);
        assertThat(DiemService.calculateGpa4(null)).isNull();
    }

    @Test
    @DisplayName("saveGradeSheet: Giảng viên không được phân công sẽ bị từ chối quyền Record Ownership")
    void saveGradeSheet_unassignedLecturer_throwsAccessDeniedException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        BangDiemLopForm form = new BangDiemLopForm();

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "GV002", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Bạn không phải là giảng viên phụ trách");
    }

    @Test
    @DisplayName("saveGradeSheet: Nhập điểm ngoài phạm vi 0.0 - 10.0 sẽ ném BusinessRuleException")
    void saveGradeSheet_invalidGradeRange_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findById(any(DangKyHocPhanId.class))).thenReturn(Optional.of(dk));

        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("23020001");
        entry.setDiemQuaTrinh(10.5); // Invalid > 10.0

        BangDiemLopForm form = new BangDiemLopForm();
        form.setEntries(List.of(entry));

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "GV001", false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("thang điểm từ 0.0 đến 10.0");
    }

    @Test
    @DisplayName("saveGradeSheet: Khi sinh viên bị Cấm thi, DiemCuoiKy phải được đặt là null để tránh vi phạm trigger 50011")
    void saveGradeSheet_camThi_setsDiemCuoiKyToNull() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findById(any(DangKyHocPhanId.class))).thenReturn(Optional.of(dk));

        com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap kq = mock(com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap.class);
        when(ketQuaHocTapRepository.findById_MaSVAndId_MaLHP("23020001", "INT2204_1")).thenReturn(Optional.of(kq));

        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("23020001");
        entry.setCamThi(true);
        entry.setDiemQuaTrinh(8.0);
        entry.setDiemGiuaKy(7.5);
        entry.setDiemCuoiKy(9.0);

        BangDiemLopForm form = new BangDiemLopForm();
        form.setEntries(List.of(entry));

        diemService.saveGradeSheet("INT2204_1", form, "GV001", false);

        verify(dk).setTrangThaiDangKy(TrangThaiDangKy.CAM_THI);
        verify(kq).setDiemCuoiKy(null);
        verify(ketQuaHocTapRepository).save(kq);
    }

    @Test
    @DisplayName("getTranscriptView: Tính tổng tín chỉ tích lũy, CPA thang 4 và xếp loại chính xác")
    void getTranscriptView_calculatesCpaAndCreditsCorrectly() {
        when(academicPolicy.passScore()).thenReturn(4.0);

        com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView v1 = mock(com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView.class);
        when(v1.getTenHK()).thenReturn("Học kỳ 1");
        when(v1.getNamHoc()).thenReturn(2023);
        when(v1.getDiemTong()).thenReturn(8.0);
        when(v1.getDiemHe4()).thenReturn(3.5);
        when(v1.getSoTinChi()).thenReturn(3);

        com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView v2 = mock(com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView.class);
        when(v2.getTenHK()).thenReturn("Học kỳ 1");
        when(v2.getNamHoc()).thenReturn(2023);
        when(v2.getDiemTong()).thenReturn(3.0); // fail score (< 4.0)

        when(bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc("SV001"))
                .thenReturn(List.of(v1, v2));

        var response = diemService.getTranscriptView("SV001");

        assertThat(response.tongTCTichLuy()).isEqualTo(3);
        assertThat(response.cpa4()).isEqualTo(3.5);
        assertThat(response.xepLoai()).isEqualTo("Giỏi");
        assertThat(response.byTerm()).containsKey("Học kỳ 1 (2023)");
        assertThat(response.byTerm().get("Học kỳ 1 (2023)")).hasSize(2);
    }

    @Test
    @DisplayName("getGradeSheet: Khi đọc từ vw_BangDiemSinhVien, mã Lớp HC vẫn được nạp đầy đủ từ sinh viên")
    void getGradeSheet_withViewData_populatesMaLopHCCorrectly() {
        com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh lhc = mock(com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh.class);
        when(lhc.getMaLopHC()).thenReturn("CNTT2023A");

        com.qldtkhoa.qldtkhoa_app.entity.SinhVien sv = mock(com.qldtkhoa.qldtkhoa_app.entity.SinhVien.class);
        when(sv.getMaSV()).thenReturn("SV66_02");
        when(sv.getLopHanhChinh()).thenReturn(lhc);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getSinhVien()).thenReturn(sv);

        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("LHP01"))
                .thenReturn(List.of(dk));

        com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView view = mock(com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView.class);
        when(view.getMaSV()).thenReturn("SV66_02");
        when(view.getHoTen()).thenReturn("Lê Tường Vy");
        when(view.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(view.getDiemTong()).thenReturn(6.1);

        when(bangDiemViewRepository.findById_MaLHPOrderByHoTenAsc("LHP01"))
                .thenReturn(List.of(view));

        var rows = diemService.getGradeSheet("LHP01");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).maSV()).isEqualTo("SV66_02");
        assertThat(rows.get(0).hoTen()).isEqualTo("Lê Tường Vy");
        assertThat(rows.get(0).maLopHC()).isEqualTo("CNTT2023A"); // Đã có mã lớp HC, không còn bị rỗng
    }
}

