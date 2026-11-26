package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhForm;
import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuyetDinhHocVuServiceTest {

    @Mock
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Mock
    private SessionInvalidationHelper sessionInvalidationHelper;

    @Mock
    private AuditService auditService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-02-01T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @InjectMocks
    private QuyetDinhHocVuService quyetDinhHocVuService;

    @Test
    @DisplayName("create: Ban hành quyết định Bảo lưu sẽ cập nhật trạng thái sinh viên sang BAO_LUU")
    void create_baoLuu_updatesStudentStatusToBaoLuu() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);

        when(quyetDinhHocVuRepository.existsById("QD001")).thenReturn(false);
        when(sinhVienRepository.findByIdForUpdate("23020001")).thenReturn(Optional.of(sv));
        when(quyetDinhHocVuRepository.findTopBySinhVien_MaSVOrderByNgayHieuLucDescMaQuyetDinhDesc("23020001"))
                .thenReturn(Optional.empty());
        when(dangKyHocPhanRepository.findActiveInSectionsWithStatus(any(), any(), any())).thenReturn(List.of());

        QuyetDinhForm form = new QuyetDinhForm();
        form.setMaQuyetDinh("QD001");
        form.setMaSV("23020001");
        form.setLoaiQuyetDinh(LoaiQuyetDinh.BAO_LUU);
        form.setNgayHieuLuc(LocalDate.of(2024, 9, 1));
        form.setLyDo("Lý do cá nhân");

        boolean pending = quyetDinhHocVuService.create(form);

        assertThat(pending).isFalse();
        verify(sv).setTrangThaiHienTai(TrangThaiSinhVien.BAO_LUU);
        verify(sinhVienRepository).save(sv);
        verify(quyetDinhHocVuRepository).saveAndFlush(any(QuyetDinhHocVu.class));
        verify(auditService).log(eq("ISSUE_DECISION"), eq("QuyetDinhHocVu"), eq("QD001"), any(), any(), eq("Lý do cá nhân"));
    }

    @Test
    @DisplayName("create: Ban hành quyết định Tốt nghiệp chuyển trạng thái SV sang DA_TOT_NGHIEP")
    void create_totNghiep_updatesStudentStatusToDaTotNghiep() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(quyetDinhHocVuRepository.existsById("QD_TN")).thenReturn(false);
        when(sinhVienRepository.findByIdForUpdate("SV63_06")).thenReturn(Optional.of(sv));
        when(quyetDinhHocVuRepository.findTopBySinhVien_MaSVOrderByNgayHieuLucDescMaQuyetDinhDesc("SV63_06"))
                .thenReturn(Optional.empty());
        when(dangKyHocPhanRepository.findActiveInSectionsWithStatus(any(), any(), any())).thenReturn(List.of());

        QuyetDinhForm form = new QuyetDinhForm();
        form.setMaQuyetDinh("QD_TN");
        form.setMaSV("SV63_06");
        form.setLoaiQuyetDinh(LoaiQuyetDinh.TOT_NGHIEP);
        form.setNgayHieuLuc(LocalDate.of(2026, 1, 20));
        form.setLyDo("Đủ điều kiện tốt nghiệp");

        quyetDinhHocVuService.create(form);

        verify(sv).setTrangThaiHienTai(TrangThaiSinhVien.DA_TOT_NGHIEP);
        verify(quyetDinhHocVuRepository).saveAndFlush(any(QuyetDinhHocVu.class));
    }

    @Test
    @DisplayName("create: Quyết định Thôi học khóa tài khoản và thu hồi phiên đăng nhập đang hoạt động")
    void create_thoiHoc_locksAccountAndExpiresSessions() {
        TaiKhoan tk = mock(TaiKhoan.class);
        when(tk.getEmail()).thenReturn("sv@student.edu.vn");
        when(tk.getTrangThaiKichHoat()).thenReturn(true);
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sv.getTaiKhoan()).thenReturn(tk);
        when(quyetDinhHocVuRepository.existsById("QD_TH")).thenReturn(false);
        when(sinhVienRepository.findByIdForUpdate("SV64_07")).thenReturn(Optional.of(sv));
        when(quyetDinhHocVuRepository.findTopBySinhVien_MaSVOrderByNgayHieuLucDescMaQuyetDinhDesc("SV64_07"))
                .thenReturn(Optional.empty());
        when(dangKyHocPhanRepository.findActiveInSectionsWithStatus(any(), any(), any())).thenReturn(List.of());

        QuyetDinhForm form = new QuyetDinhForm();
        form.setMaQuyetDinh("QD_TH");
        form.setMaSV("SV64_07");
        form.setLoaiQuyetDinh(LoaiQuyetDinh.THOI_HOC);
        form.setNgayHieuLuc(LocalDate.of(2026, 1, 15));
        form.setLyDo("Buộc thôi học");

        quyetDinhHocVuService.create(form);

        verify(sv).setTrangThaiHienTai(TrangThaiSinhVien.THOI_HOC);
        verify(tk).setTrangThaiKichHoat(false);
        verify(sessionInvalidationHelper).expireUserSessions("sv@student.edu.vn");
    }

    @Test
    @DisplayName("delete: Quyết định học vụ có tính bất biến pháp lý và không được phép xóa")
    void delete_immutableDecision_throwsBusinessRuleException() {
        assertThatThrownBy(() -> quyetDinhHocVuService.delete("QD001"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("decision.error.immutable");
    }
}
