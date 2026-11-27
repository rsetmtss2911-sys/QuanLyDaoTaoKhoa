package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaNhapHocForm;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KhoaNhapHocServiceTest {

    @Mock
    private KhoaNhapHocRepository khoaNhapHocRepository;
    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;
    @Mock
    private ChuongTrinhDaoTaoRepository ctdtRepository;

    @InjectMocks
    private KhoaNhapHocService service;

    @Test
    @DisplayName("create & update: Kiểm tra trùng mã và cập nhật năm bắt đầu")
    void createAndUpdate_workAsExpected() {
        KhoaNhapHocForm form = new KhoaNhapHocForm();
        form.setMaKhoaHoc("K68");
        form.setNamBatDau(2025);

        when(khoaNhapHocRepository.existsById("K68")).thenReturn(true);
        assertThatThrownBy(() -> service.create(form)).isInstanceOf(DuplicateResourceException.class);

        when(khoaNhapHocRepository.existsById("K68")).thenReturn(false);
        service.create(form);
        verify(khoaNhapHocRepository).save(any(KhoaNhapHoc.class));

        KhoaNhapHoc existing = mock(KhoaNhapHoc.class);
        when(khoaNhapHocRepository.findById("K68")).thenReturn(Optional.of(existing));
        service.update("K68", form);
        verify(existing).setNamBatDau(2025);
    }

    @Test
    @DisplayName("delete: Chặn xóa khi đã gắn lớp hành chính hoặc CTĐT")
    void delete_guardsDependencies() {
        KhoaNhapHoc k = mock(KhoaNhapHoc.class);
        when(k.getMaKhoaHoc()).thenReturn("K68");
        when(khoaNhapHocRepository.findById("K68")).thenReturn(Optional.of(k));
        when(lopHanhChinhRepository.existsByKhoaNhapHoc_MaKhoaHoc("K68")).thenReturn(true);

        assertThatThrownBy(() -> service.delete("K68"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("khoahoc.delete.inUse");

        when(lopHanhChinhRepository.existsByKhoaNhapHoc_MaKhoaHoc("K68")).thenReturn(false);
        when(ctdtRepository.existsByKhoaNhapHoc_MaKhoaHoc("K68")).thenReturn(false);
        service.delete("K68");

        verify(khoaNhapHocRepository).delete(k);
    }
}
