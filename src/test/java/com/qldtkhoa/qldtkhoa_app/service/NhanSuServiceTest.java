package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.GiangVienOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.NhanSuForm;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NhanSuServiceTest {

    @Mock
    private NhanSuRepository nhanSuRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    @Mock
    private VaiTroRepository vaiTroRepository;
    @Mock
    private BoMonRepository boMonRepository;
    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SessionInvalidationHelper sessionInvalidationHelper;

    @InjectMocks
    private NhanSuService service;

    @Test
    @DisplayName("create: Chặn gán vai trò SINH_VIEN và kiểm tra trùng mã/email")
    void create_validatesRoleAndDuplicates() {
        NhanSuForm svRole = new NhanSuForm();
        svRole.setMaNS("NS01");
        svRole.setEmail("ns01@edu.vn");
        svRole.setMaVaiTro("SINH_VIEN");
        assertThatThrownBy(() -> service.create(svRole))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("nhansu.vaiTro.invalid");

        NhanSuForm dupMa = new NhanSuForm();
        dupMa.setMaNS("NS01");
        dupMa.setEmail("ns01@edu.vn");
        dupMa.setMaVaiTro("ADMIN");
        when(nhanSuRepository.existsById("NS01")).thenReturn(true);
        assertThatThrownBy(() -> service.create(dupMa)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("create: Vai trò GIANG_VIEN bắt buộc bộ môn và học vị, lưu đầy đủ TaiKhoan/NhanSu/GiangVien")
    void create_giangVien_requiresDepartmentAndDegreeThenSavesAll() {
        NhanSuForm form = new NhanSuForm();
        form.setMaNS("GV01");
        form.setHoTen("Nguyễn Văn A");
        form.setEmail("GV01@edu.vn");
        form.setMaVaiTro("GIANG_VIEN");
        form.setHocVi("Tiến sĩ");
        form.setMaBoMon("CNPM");

        when(nhanSuRepository.existsById("GV01")).thenReturn(false);
        when(taiKhoanRepository.existsById("gv01@edu.vn")).thenReturn(false);
        when(vaiTroRepository.findById("GIANG_VIEN")).thenReturn(Optional.of(mock(VaiTro.class)));
        when(boMonRepository.findById("CNPM")).thenReturn(Optional.of(mock(BoMon.class)));
        when(passwordEncoder.encode("123456")).thenReturn("ENC");

        service.create(form);

        verify(taiKhoanRepository).save(any(TaiKhoan.class));
        verify(nhanSuRepository).save(any(NhanSu.class));
        verify(giangVienRepository).save(any(GiangVien.class));
    }

    @Test
    @DisplayName("delete: Chặn tự xóa chính mình hoặc xóa ADMIN đang hoạt động cuối cùng")
    void delete_guardsSelfDeleteAndLastAdmin() {
        VaiTro vt = mock(VaiTro.class);
        when(vt.getMaVaiTro()).thenReturn("ADMIN");
        TaiKhoan tk = mock(TaiKhoan.class);
        when(tk.getEmail()).thenReturn("admin@edu.vn");
        when(tk.getVaiTro()).thenReturn(vt);
        when(tk.getTrangThaiKichHoat()).thenReturn(true);
        NhanSu ns = mock(NhanSu.class);
        when(ns.getTaiKhoan()).thenReturn(tk);
        when(nhanSuRepository.findById("AD01")).thenReturn(Optional.of(ns));

        assertThatThrownBy(() -> service.delete("AD01", "ADMIN@edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("nhansu.error.selfDelete");

        when(taiKhoanRepository.countByVaiTro_MaVaiTroAndTrangThaiKichHoat("ADMIN", true)).thenReturn(1L);
        assertThatThrownBy(() -> service.delete("AD01", "other@edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("nhansu.error.lastAdmin");
    }

    @Test
    @DisplayName("delete: Xóa thành công sẽ flush và thu hồi session")
    void delete_happyPath_deletesAndExpiresSession() {
        VaiTro vt = mock(VaiTro.class);
        when(vt.getMaVaiTro()).thenReturn("GIANG_VIEN");
        TaiKhoan tk = mock(TaiKhoan.class);
        when(tk.getEmail()).thenReturn("gv@edu.vn");
        when(tk.getVaiTro()).thenReturn(vt);
        NhanSu ns = mock(NhanSu.class);
        when(ns.getTaiKhoan()).thenReturn(tk);
        when(nhanSuRepository.findById("GV01")).thenReturn(Optional.of(ns));
        when(lopHanhChinhRepository.existsByCoVan_MaNS("GV01")).thenReturn(false);
        when(lopHocPhanRepository.existsByGiangVien_MaNS("GV01")).thenReturn(false);

        service.delete("GV01", "admin@edu.vn");

        verify(giangVienRepository).deleteById("GV01");
        verify(nhanSuRepository).delete(ns);
        verify(taiKhoanRepository).delete(tk);
        verify(sessionInvalidationHelper).expireUserSessions("gv@edu.vn");
    }

    @Test
    @DisplayName("findAllGiangVienOptions: Ánh xạ GiangVien sang GiangVienOptionRow")
    void findAllGiangVienOptions_mapsDto() {
        NhanSu ns = mock(NhanSu.class);
        when(ns.getHoTen()).thenReturn("TS. Lê C");
        BoMon bm = mock(BoMon.class);
        when(bm.getTenBoMon()).thenReturn("KHMT");
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV03");
        when(gv.getNhanSu()).thenReturn(ns);
        when(gv.getBoMon()).thenReturn(bm);
        when(giangVienRepository.findAllByOrderByNhanSu_HoTenAsc()).thenReturn(List.of(gv));

        List<GiangVienOptionRow> options = service.findAllGiangVienOptions();

        assertThat(options).containsExactly(new GiangVienOptionRow("GV03", "TS. Lê C", "KHMT"));
    }
}
