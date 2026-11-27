package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.NganhHocForm;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
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
class NganhHocServiceTest {

    @Mock
    private NganhHocRepository nganhHocRepository;
    @Mock
    private KhoaRepository khoaRepository;
    @Mock
    private ChuongTrinhDaoTaoRepository ctdtRepository;
    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;

    @InjectMocks
    private NganhHocService service;

    @Test
    @DisplayName("create: Chặn trùng mã ngành và lưu khi hợp lệ")
    void create_handlesDuplicateAndSuccess() {
        NganhHocForm form = new NganhHocForm();
        form.setMaNganh("CNTT");
        form.setTenNganh("Công nghệ thông tin");
        form.setMaKhoa("K_CNTT");

        when(nganhHocRepository.existsById("CNTT")).thenReturn(true);
        assertThatThrownBy(() -> service.create(form)).isInstanceOf(DuplicateResourceException.class);

        when(nganhHocRepository.existsById("CNTT")).thenReturn(false);
        when(khoaRepository.findById("K_CNTT")).thenReturn(Optional.of(mock(Khoa.class)));
        service.create(form);

        verify(nganhHocRepository).save(any(NganhHoc.class));
    }

    @Test
    @DisplayName("delete: Chặn xóa khi đã gắn với CTĐT hoặc lớp hành chính")
    void delete_guardsDependencies() {
        NganhHoc n = mock(NganhHoc.class);
        when(n.getTenNganh()).thenReturn("CNTT");
        when(nganhHocRepository.findById("CNTT")).thenReturn(Optional.of(n));
        when(ctdtRepository.existsByNganhHoc_MaNganh("CNTT")).thenReturn(true);

        assertThatThrownBy(() -> service.delete("CNTT"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("nganh.delete.inUse");

        when(ctdtRepository.existsByNganhHoc_MaNganh("CNTT")).thenReturn(false);
        when(lopHanhChinhRepository.existsByNganhHoc_MaNganh("CNTT")).thenReturn(false);
        service.delete("CNTT");

        verify(nganhHocRepository).delete(n);
    }
}
