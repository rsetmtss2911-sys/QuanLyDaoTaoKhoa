package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhForm;
import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuyetDinhHocVuServiceTest {

    @Mock
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @InjectMocks
    private QuyetDinhHocVuService quyetDinhHocVuService;

    @Test
    @DisplayName("create: Ban hành quyết định Bảo lưu sẽ cập nhật trạng thái sinh viên sang BAO_LUU")
    void create_baoLuu_updatesStudentStatusToBaoLuu() {
        SinhVien sv = mock(SinhVien.class);

        when(quyetDinhHocVuRepository.existsById("QD001")).thenReturn(false);
        when(sinhVienRepository.findById("23020001")).thenReturn(Optional.of(sv));

        QuyetDinhForm form = new QuyetDinhForm();
        form.setMaQuyetDinh("QD001");
        form.setMaSV("23020001");
        form.setLoaiQuyetDinh(LoaiQuyetDinh.BAO_LUU);
        form.setNgayHieuLuc(LocalDate.of(2024, 9, 1));
        form.setLyDo("Lý do cá nhân");

        quyetDinhHocVuService.create(form);

        verify(sv).setTrangThaiHienTai(TrangThaiSinhVien.BAO_LUU);
        verify(sinhVienRepository).save(sv);
        verify(quyetDinhHocVuRepository).save(any(QuyetDinhHocVu.class));
    }

    @Test
    @DisplayName("delete: Quyết định học vụ có tính bất biến pháp lý và không được phép xóa")
    void delete_immutableDecision_throwsBusinessRuleException() {
        assertThatThrownBy(() -> quyetDinhHocVuService.delete("QD001"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("decision.error.immutable");
    }
}
