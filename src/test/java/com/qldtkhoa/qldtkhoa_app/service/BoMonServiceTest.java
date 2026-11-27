package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.BoMonForm;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.GiangVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
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
class BoMonServiceTest {

    @Mock
    private BoMonRepository boMonRepository;
    @Mock
    private KhoaRepository khoaRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private MonHocRepository monHocRepository;

    @InjectMocks
    private BoMonService service;

    @Test
    @DisplayName("create: Trùng mã bộ môn ném DuplicateResourceException, hợp lệ sẽ lưu")
    void create_handlesDuplicateAndSuccess() {
        BoMonForm form = new BoMonForm();
        form.setMaBoMon("CNPM");
        form.setTenBoMon("Công nghệ phần mềm");
        form.setMaKhoa("CNTT");

        when(boMonRepository.existsById("CNPM")).thenReturn(true);
        assertThatThrownBy(() -> service.create(form)).isInstanceOf(DuplicateResourceException.class);

        when(boMonRepository.existsById("CNPM")).thenReturn(false);
        when(khoaRepository.findById("CNTT")).thenReturn(Optional.of(mock(Khoa.class)));
        service.create(form);

        verify(boMonRepository).save(any(BoMon.class));
    }

    @Test
    @DisplayName("delete: Chặn xóa khi còn giảng viên hoặc môn học tham chiếu")
    void delete_guardsDependencies() {
        BoMon bm = mock(BoMon.class);
        when(bm.getTenBoMon()).thenReturn("CNPM");
        when(boMonRepository.findById("CNPM")).thenReturn(Optional.of(bm));
        when(giangVienRepository.existsByBoMon_MaBoMon("CNPM")).thenReturn(true);

        assertThatThrownBy(() -> service.delete("CNPM"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("bomon.delete.inUse");

        when(giangVienRepository.existsByBoMon_MaBoMon("CNPM")).thenReturn(false);
        when(monHocRepository.existsByBoMon_MaBoMon("CNPM")).thenReturn(false);
        service.delete("CNPM");

        verify(boMonRepository).delete(bm);
    }
}
