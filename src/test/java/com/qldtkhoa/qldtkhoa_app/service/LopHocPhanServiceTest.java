package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LopHocPhanServiceTest {

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;
    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private HocKyNamHocRepository hocKyRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Mock
    private LichGiangDayRepository lichGiangDayRepository;
    @Mock
    private KetQuaHocTapRepository ketQuaHocTapRepository;
    @Mock
    private AcademicPolicy academicPolicy;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private LopHocPhanService lopHocPhanService;

    private LopHocPhan activeLhp;
    private HocKyNamHoc hocKy;
    private GiangVien giangVien;
    private NhanSu nhanSu;
    private MonHoc monHoc;

    @BeforeEach
    void setUp() {
        hocKy = mock(HocKyNamHoc.class);
        lenient().when(hocKy.getMaHK()).thenReturn("HK1_2025");

        nhanSu = mock(NhanSu.class);
        lenient().when(nhanSu.getMaNS()).thenReturn("NS001");
        lenient().when(nhanSu.getHoTen()).thenReturn("Nguyễn Văn A");

        giangVien = mock(GiangVien.class);
        lenient().when(giangVien.getMaNS()).thenReturn("NS001");
        lenient().when(giangVien.getNhanSu()).thenReturn(nhanSu);

        monHoc = mock(MonHoc.class);
        lenient().when(monHoc.getMaMon()).thenReturn("INT2201");
        lenient().when(monHoc.getTenMon()).thenReturn("Cấu trúc dữ liệu");
        lenient().when(monHoc.getSoTinChi()).thenReturn(3);

        activeLhp = mock(LopHocPhan.class);
        lenient().when(activeLhp.getMaLHP()).thenReturn("LHP01");
        lenient().when(activeLhp.getHocKy()).thenReturn(hocKy);
        lenient().when(activeLhp.getGiangVien()).thenReturn(giangVien);
        lenient().when(activeLhp.getMonHoc()).thenReturn(monHoc);
        lenient().when(activeLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);

        lenient().when(lopHocPhanRepository.findById("LHP01")).thenReturn(Optional.of(activeLhp));
        lenient().when(lopHocPhanRepository.findByIdWithDetails("LHP01")).thenReturn(Optional.of(activeLhp));
    }

    @Test
    @DisplayName("addSchedule should save successfully when there is no conflict")
    void addSchedule_validInput_savesSuccessfully() {
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01")).thenReturn(List.of());
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01")).thenReturn(List.of());
        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc("HK1_2025", "301-G2")).thenReturn(List.of());
        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS("HK1_2025", "NS001")).thenReturn(List.of());

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setPhongHoc("301-G2");

        lopHocPhanService.addSchedule("LHP01", form);

        verify(lichGiangDayRepository, times(1)).save(any(LichGiangDay.class));
    }

    @Test
    @DisplayName("addSchedule should throw exception if section is DA_HUY")
    void addSchedule_cancelledClass_throwsBusinessRuleException() {
        when(activeLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_HUY);

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setPhongHoc("301-G2");

        assertThatThrownBy(() -> lopHocPhanService.addSchedule("LHP01", form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.schedule.lockedState");

        verify(lichGiangDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSchedule should throw exception when start period is greater than end period")
    void addSchedule_invalidPeriods_throwsBusinessRuleException() {

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(5);
        form.setTietKetThuc(3); // Start > End
        form.setPhongHoc("301-G2");

        assertThatThrownBy(() -> lopHocPhanService.addSchedule("LHP01", form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.schedule.invalidPeriods");
    }

    @Test
    @DisplayName("addSchedule should throw exception on room conflict (Rule 50020)")
    void addSchedule_roomConflict_throwsBusinessRuleException() {
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01")).thenReturn(List.of());

        LopHocPhan otherLhp = mock(LopHocPhan.class);
        when(otherLhp.getMaLHP()).thenReturn("LHP02");
        when(otherLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);

        LichGiangDay otherSchedule = mock(LichGiangDay.class);
        when(otherSchedule.getLopHocPhan()).thenReturn(otherLhp);
        when(otherSchedule.getThuTrongTuan()).thenReturn(2);
        when(otherSchedule.getTietBatDau()).thenReturn(2);
        when(otherSchedule.getTietKetThuc()).thenReturn(4);

        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc("HK1_2025", "301-G2"))
                .thenReturn(List.of(otherSchedule));

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3); // Overlaps 1-3 with 2-4
        form.setPhongHoc("301-G2");

        assertThatThrownBy(() -> lopHocPhanService.addSchedule("LHP01", form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.schedule.roomConflict");

        verify(lichGiangDayRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSchedule should ignore room conflict if the other section is DA_HUY")
    void addSchedule_roomConflictCancelledClass_ignoresAndSucceeds() {
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01")).thenReturn(List.of());

        LopHocPhan cancelledLhp = mock(LopHocPhan.class);
        when(cancelledLhp.getMaLHP()).thenReturn("LHP_CANCELLED");
        when(cancelledLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_HUY);

        LichGiangDay cancelledSchedule = mock(LichGiangDay.class);
        when(cancelledSchedule.getLopHocPhan()).thenReturn(cancelledLhp);

        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc("HK1_2025", "301-G2"))
                .thenReturn(List.of(cancelledSchedule));
        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS("HK1_2025", "NS001"))
                .thenReturn(List.of());

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setPhongHoc("301-G2");

        lopHocPhanService.addSchedule("LHP01", form);

        verify(lichGiangDayRepository, times(1)).save(any(LichGiangDay.class));
    }

    @Test
    @DisplayName("addSchedule should throw exception on lecturer conflict (Rule 50021)")
    void addSchedule_lecturerConflict_throwsBusinessRuleException() {
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01")).thenReturn(List.of());
        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc("HK1_2025", "301-G2")).thenReturn(List.of());

        LopHocPhan otherLhp = mock(LopHocPhan.class);
        when(otherLhp.getMaLHP()).thenReturn("LHP99");
        when(otherLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);

        LichGiangDay otherGvSchedule = mock(LichGiangDay.class);
        when(otherGvSchedule.getLopHocPhan()).thenReturn(otherLhp);
        when(otherGvSchedule.getThuTrongTuan()).thenReturn(2);
        when(otherGvSchedule.getTietBatDau()).thenReturn(3);
        when(otherGvSchedule.getTietKetThuc()).thenReturn(5);

        when(lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS("HK1_2025", "NS001"))
                .thenReturn(List.of(otherGvSchedule));

        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3); // Overlaps at period 3
        form.setPhongHoc("301-G2");

        assertThatThrownBy(() -> lopHocPhanService.addSchedule("LHP01", form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.schedule.lecturerConflict");
    }

    @Test
    @DisplayName("removeSchedule should delete schedule if it exists")
    void removeSchedule_existingSchedule_deletesById() {
        when(lichGiangDayRepository.existsById(any())).thenReturn(true);

        lopHocPhanService.removeSchedule("LHP01", 2, 1);

        verify(lichGiangDayRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("lockEnrollment: Chặn chốt danh sách nếu chưa xếp lịch hoặc chưa đủ sĩ số tối thiểu")
    void lockEnrollment_noScheduleOrBelowMinStudents_throwsBusinessRuleException() {
        when(lopHocPhanRepository.findByIdForUpdate("LHP01")).thenReturn(Optional.of(activeLhp));
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> lopHocPhanService.lockEnrollment("LHP01", "pdt@faculty.edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.lock.noSchedule");

        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01"))
                .thenReturn(List.of(mock(LichGiangDay.class)));
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(eq("LHP01"), anyCollection()))
                .thenReturn(2L);
        when(academicPolicy.minSectionStudents()).thenReturn(5);

        assertThatThrownBy(() -> lopHocPhanService.lockEnrollment("LHP01", "pdt@faculty.edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.lock.belowMinStudents");
    }

    @Test
    @DisplayName("lockEnrollment & lockGrades: Chuyển trạng thái CHO_MO -> DA_CHOT_DANH_SACH -> DA_KHOA_DIEM và ghi audit")
    void lockEnrollmentAndGrades_validLifecycle_transitionsAndAudits() {
        when(lopHocPhanRepository.findByIdForUpdate("LHP01")).thenReturn(Optional.of(activeLhp));
        when(lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc("LHP01"))
                .thenReturn(List.of(mock(LichGiangDay.class)));
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(eq("LHP01"), anyCollection()))
                .thenReturn(15L);
        when(academicPolicy.minSectionStudents()).thenReturn(5);

        lopHocPhanService.lockEnrollment("LHP01", "pdt@faculty.edu.vn");
        verify(activeLhp).setTrangThaiLop(TrangThaiLop.DA_CHOT_DANH_SACH);
        verify(auditService).logAs(eq("pdt@faculty.edu.vn"), eq("LOCK_ENROLLMENT"),
                eq("LopHocPhan"), eq("LHP01"), any(), any(), any());

        when(activeLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_CHOT_DANH_SACH);
        lopHocPhanService.lockGrades("LHP01", "pdt@faculty.edu.vn");
        verify(activeLhp).setTrangThaiLop(TrangThaiLop.DA_KHOA_DIEM);
        verify(auditService).logAs(eq("pdt@faculty.edu.vn"), eq("LOCK_GRADES"),
                eq("LopHocPhan"), eq("LHP01"), any(), any(), any());
    }

    @Test
    @DisplayName("cancelSection: Chặn hủy lớp nếu đã có điểm; lớp chưa có điểm sẽ rút toàn bộ SV và hủy lớp")
    void cancelSection_hasGradesOrValid_enforcesRuleAndWithdrawsStudents() {
        when(lopHocPhanRepository.findByIdForUpdate("LHP01")).thenReturn(Optional.of(activeLhp));
        when(ketQuaHocTapRepository.existsByDangKy_LopHocPhan_MaLHP("LHP01")).thenReturn(true);

        assertThatThrownBy(() -> lopHocPhanService.cancelSection("LHP01", "Không đủ sĩ số", "pdt@faculty.edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("lophp.cancel.hasGrades");

        when(ketQuaHocTapRepository.existsByDangKy_LopHocPhan_MaLHP("LHP01")).thenReturn(false);
        DangKyHocPhan dk1 = mock(DangKyHocPhan.class);
        when(dk1.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc("LHP01"))
                .thenReturn(List.of(dk1));

        int withdrawn = lopHocPhanService.cancelSection("LHP01", "Không mở được lớp", "pdt@faculty.edu.vn");

        assertThat(withdrawn).isEqualTo(1);
        verify(dk1).setTrangThaiDangKy(TrangThaiDangKy.DA_RUT_LOP);
        verify(activeLhp).setTrangThaiLop(TrangThaiLop.DA_HUY);
        verify(auditService).logAs(eq("pdt@faculty.edu.vn"), eq("CANCEL_SECTION"),
                eq("LopHocPhan"), eq("LHP01"), any(), any(), any());
    }
}
