package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaForm;
import com.qldtkhoa.qldtkhoa_app.dto.KhoaRow;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KhoaServiceTest {

    @Mock
    private KhoaRepository khoaRepository;

    @Mock
    private BoMonRepository boMonRepository;

    @Mock
    private NganhHocRepository nganhHocRepository;

    @InjectMocks
    private KhoaService khoaService;

    @Test
    @DisplayName("search: Sử dụng countByKhoa_MaKhoa thay vì load entities, trả về Page<KhoaRow> chính xác")
    void search_usesCountQueries_returnsCorrectPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Khoa k1 = new Khoa("CNTT", "Công nghệ Thông tin", 1995);
        Page<Khoa> page = new PageImpl<>(List.of(k1), pageable, 1);

        when(khoaRepository.search("CNTT", pageable)).thenReturn(page);
        when(boMonRepository.countByKhoa_MaKhoa("CNTT")).thenReturn(4L);
        when(nganhHocRepository.countByKhoa_MaKhoa("CNTT")).thenReturn(2L);

        Page<KhoaRow> result = khoaService.search("CNTT", pageable);

        assertThat(result.getContent()).hasSize(1);
        KhoaRow row = result.getContent().get(0);
        assertThat(row.maKhoa()).isEqualTo("CNTT");
        assertThat(row.tenKhoa()).isEqualTo("Công nghệ Thông tin");
        assertThat(row.soBoMon()).isEqualTo(4L);
        assertThat(row.soNganh()).isEqualTo(2L);

        verify(boMonRepository).countByKhoa_MaKhoa("CNTT");
        verify(nganhHocRepository).countByKhoa_MaKhoa("CNTT");
    }

    @Test
    @DisplayName("create: Mã khoa trùng lặp ném DuplicateResourceException")
    void create_duplicateMaKhoa_throwsDuplicateResourceException() {
        KhoaForm form = new KhoaForm();
        form.setMaKhoa("CNTT");
        form.setTenKhoa("Công nghệ Thông tin");

        when(khoaRepository.existsById("CNTT")).thenReturn(true);

        assertThatThrownBy(() -> khoaService.create(form))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("khoa.maKhoa.duplicate");
    }

    @Test
    @DisplayName("delete: Khoa đang có bộ môn hoặc ngành học liên kết sẽ ném BusinessRuleException")
    void delete_inUse_throwsBusinessRuleException() {
        Khoa k = new Khoa("CNTT", "Công nghệ Thông tin", 1995);
        when(khoaRepository.findById("CNTT")).thenReturn(Optional.of(k));
        when(boMonRepository.existsByKhoa_MaKhoa("CNTT")).thenReturn(true);

        assertThatThrownBy(() -> khoaService.delete("CNTT"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
