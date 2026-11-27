package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.VaiTroRow;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.repository.VaiTroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaiTroServiceTest {

    @Mock
    private VaiTroRepository vaiTroRepository;

    @InjectMocks
    private VaiTroService service;

    @Test
    @DisplayName("findAll: Ánh xạ toàn bộ VaiTro sang VaiTroRow")
    void findAll_mapsToVaiTroRows() {
        VaiTro vt1 = mock(VaiTro.class);
        when(vt1.getMaVaiTro()).thenReturn("ADMIN");
        when(vt1.getTenVaiTro()).thenReturn("Quản trị viên");
        VaiTro vt2 = mock(VaiTro.class);
        when(vt2.getMaVaiTro()).thenReturn("SINH_VIEN");
        when(vt2.getTenVaiTro()).thenReturn("Sinh viên");
        when(vaiTroRepository.findAll()).thenReturn(List.of(vt1, vt2));

        List<VaiTroRow> rows = service.findAll();

        assertThat(rows).containsExactly(
                new VaiTroRow("ADMIN", "Quản trị viên"),
                new VaiTroRow("SINH_VIEN", "Sinh viên")
        );
    }
}
