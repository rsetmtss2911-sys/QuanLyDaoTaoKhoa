package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KetQuaHocTapRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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
    private AcademicPolicy academicPolicy;

    @Mock
    private AuditService auditService;

    private DiemService diemService;

    @BeforeEach
    void setUp() {
        HocLucService hocLucService = new HocLucService(bangDiemViewRepository, academicPolicy);
        diemService = new DiemService(
                lopHocPhanRepository, dangKyHocPhanRepository, ketQuaHocTapRepository,
                bangDiemViewRepository, hocLucService, auditService
        );
    }

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
    @DisplayName("saveGradeSheet: Lớp chưa chốt danh sách hoặc đã khóa điểm thì giảng viên không được nhập điểm")
    void saveGradeSheet_sectionNotLockedForGrading_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        BangDiemLopForm form = new BangDiemLopForm();

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "GV001", false))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("grade.classNotLocked");
    }

    @Test
    @DisplayName("saveGradeSheet: Nhập điểm ngoài phạm vi 0.0 - 10.0 hoặc NaN sẽ ném BusinessRuleException")
    void saveGradeSheet_invalidGradeRangeOrNaN_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_CHOT_DANH_SACH);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getId()).thenReturn(new DangKyHocPhanId("23020001", "INT2204_1"));
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("INT2204_1"))
                .thenReturn(List.of(dk));
        when(ketQuaHocTapRepository.findById_MaLHP("INT2204_1")).thenReturn(List.of());

        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("23020001");
        entry.setDiemQuaTrinh(Double.NaN);

        BangDiemLopForm form = new BangDiemLopForm();
        form.setEntries(List.of(entry));

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "GV001", false))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("grade.scoreRange");
    }

    @Test
    @DisplayName("saveGradeSheet: Khi sinh viên bị Cấm thi, DiemCuoiKy = null và flush DangKyHocPhan trước KetQuaHocTap")
    void saveGradeSheet_camThi_setsDiemCuoiKyToNullAndFlushesRegistrationsFirst() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_CHOT_DANH_SACH);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhanId id = new DangKyHocPhanId("23020001", "INT2204_1");
        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getId()).thenReturn(id);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("INT2204_1"))
                .thenReturn(List.of(dk));

        KetQuaHocTap kq = mock(KetQuaHocTap.class);
        when(kq.getId()).thenReturn(id);
        when(ketQuaHocTapRepository.findById_MaLHP("INT2204_1")).thenReturn(List.of(kq));

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

        InOrder inOrder = inOrder(dangKyHocPhanRepository, ketQuaHocTapRepository);
        inOrder.verify(dangKyHocPhanRepository).saveAllAndFlush(anyList());
        inOrder.verify(ketQuaHocTapRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("getTranscriptView: Tính tổng tín chỉ tích lũy, CPA thang 4 và xếp loại chính xác")
    void getTranscriptView_calculatesCpaAndCreditsCorrectly() {
        when(academicPolicy.passScore()).thenReturn(4.0);

        BangDiemSinhVienView v1 = mock(BangDiemSinhVienView.class);
        when(v1.getMaMon()).thenReturn("INT1001");
        when(v1.getTenHK()).thenReturn("Học kỳ 1");
        when(v1.getNamHoc()).thenReturn(2023);
        when(v1.getDiemTong()).thenReturn(8.0);
        when(v1.getDiemHe4()).thenReturn(3.5);
        when(v1.getSoTinChi()).thenReturn(3);
        when(v1.getTinhVaoGPA()).thenReturn(true);

        BangDiemSinhVienView v2 = mock(BangDiemSinhVienView.class);
        when(v2.getMaMon()).thenReturn("INT1002");
        when(v2.getTenHK()).thenReturn("Học kỳ 1");
        when(v2.getNamHoc()).thenReturn(2023);
        when(v2.getDiemTong()).thenReturn(3.0);
        when(v2.getTinhVaoGPA()).thenReturn(true);

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

        BangDiemSinhVienView view = mock(BangDiemSinhVienView.class);
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
        assertThat(rows.get(0).maLopHC()).isEqualTo("CNTT2023A");
    }

    @Test
    @DisplayName("saveGradeSheet (GATE 3c): Lớp Đã khóa điểm chặn Giảng viên sửa điểm")
    void saveGradeSheet_lockedGradesByLecturer_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV001");
        when(lhp.getGiangVien()).thenReturn(gv);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_KHOA_DIEM);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        BangDiemLopForm form = new BangDiemLopForm();

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "GV001", false))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("grade.classGradesLocked");
    }

    @Test
    @DisplayName("saveGradeSheet (GATE 3c): Phòng Đào tạo sửa điểm lớp Đã khóa điểm thiếu lý do sẽ bị từ chối")
    void saveGradeSheet_lockedGradesByPdtWithoutReason_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_KHOA_DIEM);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        BangDiemLopForm form = new BangDiemLopForm();
        form.setLyDoChinhSua("   ");

        assertThatThrownBy(() -> diemService.saveGradeSheet("INT2204_1", form, "pdt@faculty.edu.vn", true))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("grade.editReason.required");
    }

    @Test
    @DisplayName("saveGradeSheet (GATE 3c): Phòng Đào tạo sửa điểm lớp Đã khóa điểm có lý do thành công và ghi NhatKyHeThong")
    void saveGradeSheet_lockedGradesByPdtWithReason_savesAndAudits() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_KHOA_DIEM);
        when(lopHocPhanRepository.findById("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhanId id = new DangKyHocPhanId("23020001", "INT2204_1");
        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getId()).thenReturn(id);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("INT2204_1"))
                .thenReturn(List.of(dk));

        KetQuaHocTap kq = mock(KetQuaHocTap.class);
        when(kq.getId()).thenReturn(id);
        when(kq.getDiemQuaTrinh()).thenReturn(7.0);
        when(kq.getDiemGiuaKy()).thenReturn(7.0);
        when(kq.getDiemCuoiKy()).thenReturn(6.0);
        when(ketQuaHocTapRepository.findById_MaLHP("INT2204_1")).thenReturn(List.of(kq));

        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("23020001");
        entry.setDiemQuaTrinh(7.0);
        entry.setDiemGiuaKy(7.0);
        entry.setDiemCuoiKy(8.5);

        BangDiemLopForm form = new BangDiemLopForm();
        form.setLyDoChinhSua("Phúc khảo bài thi theo biên bản số 12");
        form.setEntries(List.of(entry));

        diemService.saveGradeSheet("INT2204_1", form, "pdt@faculty.edu.vn", true);

        verify(kq).setDiemCuoiKy(8.5);
        verify(ketQuaHocTapRepository).saveAllAndFlush(anyList());
        verify(auditService).log(
                eq("EDIT_LOCKED_GRADE"),
                eq("KetQuaHocTap"),
                eq("INT2204_1:23020001"),
                anyString(),
                anyString(),
                eq("Phúc khảo bài thi theo biên bản số 12")
        );
    }

    @Test
    @DisplayName("getGradeSheet: Không NPE khi sinh viên chưa có điểm tổng (diemTong = null) ở nhánh View")
    void getGradeSheet_ungradedStudentInView_returnsNullScoresWithoutNpe() {
        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("INT2204_1"))
                .thenReturn(List.of());
        BangDiemSinhVienView view = mock(BangDiemSinhVienView.class);
        when(view.getMaSV()).thenReturn("23020001");
        when(view.getHoTen()).thenReturn("Nguyễn Văn A");
        when(view.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(view.getDiemTong()).thenReturn(null);
        when(view.getDiemHe4()).thenReturn(null);
        when(view.getDiemChu()).thenReturn(null);
        when(bangDiemViewRepository.findById_MaLHPOrderByHoTenAsc("INT2204_1")).thenReturn(List.of(view));

        List<DiemRow> rows = diemService.getGradeSheet("INT2204_1");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).diemTong()).isNull();
        assertThat(rows.get(0).diemHe4()).isNull();
        assertThat(rows.get(0).diemChu()).isEqualTo("—");
    }

    @Test
    @DisplayName("getGradeSheet: Không NPE khi sinh viên chưa có kết quả điểm ở nhánh fallback")
    void getGradeSheet_ungradedStudentInFallback_returnsNullScoresWithoutNpe() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("23020001");
        when(sv.getHoTen()).thenReturn("Nguyễn Văn A");
        when(sv.getLopHanhChinh()).thenReturn(null);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getSinhVien()).thenReturn(sv);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dk.getLopHocPhan()).thenReturn(null);

        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("INT2204_1"))
                .thenReturn(List.of(dk));
        when(bangDiemViewRepository.findById_MaLHPOrderByHoTenAsc("INT2204_1")).thenReturn(List.of());
        when(ketQuaHocTapRepository.findById_MaLHP("INT2204_1")).thenReturn(List.of());

        List<DiemRow> rows = diemService.getGradeSheet("INT2204_1");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).diemTong()).isNull();
        assertThat(rows.get(0).diemHe4()).isNull();
        assertThat(rows.get(0).diemChu()).isEqualTo("—");
    }
}
