package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangKyHocPhanServiceTest {

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private DieuKienMonHocRepository dieuKienMonHocRepository;

    @Mock
    private KetQuaHocTapRepository ketQuaHocTapRepository;

    @Mock
    private LichGiangDayRepository lichGiangDayRepository;

    @Mock
    private ThongKeLopHocPhanViewRepository thongKeLopHocPhanViewRepository;

    @Mock
    private ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;

    @Mock
    private HocKyNamHocRepository hocKyNamHocRepository;

    @Mock
    private AcademicPolicy academicPolicy;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2024-09-05T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @InjectMocks
    private DangKyHocPhanService dangKyHocPhanService;

    @Test
    @DisplayName("register: Chưa đạt môn tiên quyết sẽ bị từ chối đăng ký")
    void register_prerequisiteNotMet_throwsBusinessRuleException() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sinhVienRepository.findByIdForUpdate("23020001")).thenReturn(Optional.of(sv));

        MonHoc mh = mock(MonHoc.class);
        when(mh.getMaMon()).thenReturn("INT2204");

        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hk.getMaHK()).thenReturn("HK1_2024");

        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lhp.getMonHoc()).thenReturn(mh);
        when(lhp.getHocKy()).thenReturn(hk);

        when(lopHocPhanRepository.findByIdForUpdate("INT2204_1")).thenReturn(Optional.of(lhp));
        when(dangKyHocPhanRepository.findById(any())).thenReturn(Optional.empty());
        when(dangKyHocPhanRepository.existsActiveCourseInTerm(
                eq("23020001"), eq("INT2204"), eq("HK1_2024"), anyCollection(), eq(TrangThaiLop.DA_HUY)
        )).thenReturn(false);

        MonHoc mhPrereq = mock(MonHoc.class);
        when(mhPrereq.getMaMon()).thenReturn("INT1001");
        when(mhPrereq.getTenMon()).thenReturn("Tin học đại cương");

        DieuKienMonHoc dk = mock(DieuKienMonHoc.class);
        when(dk.getMonTienQuyet()).thenReturn(mhPrereq);

        when(dieuKienMonHocRepository.findByMonHoc_MaMon("INT2204")).thenReturn(List.of(dk));
        when(academicPolicy.passScore()).thenReturn(4.0);
        when(ketQuaHocTapRepository.hasPassedSubjectBeforeTerm(
                eq("23020001"), eq("INT1001"), eq(4.0), any(), eq(TrangThaiDangKy.HOP_LE), eq(TrangThaiLop.DA_HUY)
        )).thenReturn(false);

        assertThatThrownBy(() -> dangKyHocPhanService.register("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.prerequisiteMissing");
    }

    @Test
    @DisplayName("register: Sinh viên đang bị Cấm thi ở lớp này không được tự đăng ký lại để gỡ Cấm thi")
    void register_examBannedStudent_throwsBusinessRuleException() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sinhVienRepository.findByIdForUpdate("23020001")).thenReturn(Optional.of(sv));

        MonHoc mh = mock(MonHoc.class);
        when(mh.getMaMon()).thenReturn("INT2204");
        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hk.getMaHK()).thenReturn("HK1_2024");
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lhp.getMonHoc()).thenReturn(mh);
        when(lhp.getHocKy()).thenReturn(hk);
        when(lopHocPhanRepository.findByIdForUpdate("INT2204_1")).thenReturn(Optional.of(lhp));

        DangKyHocPhan banned = mock(DangKyHocPhan.class);
        when(banned.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.CAM_THI);
        when(dangKyHocPhanRepository.findById(new DangKyHocPhanId("23020001", "INT2204_1")))
                .thenReturn(Optional.of(banned));

        assertThatThrownBy(() -> dangKyHocPhanService.register("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.error.examBannedReRegister");
    }

    @Test
    @DisplayName("register: Vượt quá giới hạn tín chỉ tối đa trong kỳ sẽ ném BusinessRuleException")
    void register_exceedMaxCredits_throwsBusinessRuleException() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sinhVienRepository.findByIdForUpdate("23020001")).thenReturn(Optional.of(sv));

        MonHoc mh = mock(MonHoc.class);
        when(mh.getMaMon()).thenReturn("INT2204");
        when(mh.getSoTinChi()).thenReturn(3);

        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hk.getMaHK()).thenReturn("HK1_2024");

        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lhp.getMonHoc()).thenReturn(mh);
        when(lhp.getHocKy()).thenReturn(hk);

        when(lopHocPhanRepository.findByIdForUpdate("INT2204_1")).thenReturn(Optional.of(lhp));
        when(dangKyHocPhanRepository.findById(any(DangKyHocPhanId.class))).thenReturn(Optional.empty());
        when(dieuKienMonHocRepository.findByMonHoc_MaMon("INT2204")).thenReturn(List.of());

        MonHoc mhOld = mock(MonHoc.class);
        when(mhOld.getSoTinChi()).thenReturn(24);
        LopHocPhan lhpOld = mock(LopHocPhan.class);
        when(lhpOld.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lhpOld.getMonHoc()).thenReturn(mhOld);
        DangKyHocPhan curDk = mock(DangKyHocPhan.class);
        when(curDk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(curDk.getLopHocPhan()).thenReturn(lhpOld);

        when(dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK("23020001", "HK1_2024"))
                .thenReturn(List.of(curDk));
        when(academicPolicy.maxCreditsPerTerm()).thenReturn(25);

        assertThatThrownBy(() -> dangKyHocPhanService.register("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.maxCreditsExceeded");
    }

    @Test
    @DisplayName("register: Khóa SinhVien trước LopHocPhan theo thứ tự nhất quán và lưu bằng saveAndFlush")
    void register_validRequest_locksStudentThenSectionAndFlushes() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("23020001");
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sinhVienRepository.findByIdForUpdate("23020001")).thenReturn(Optional.of(sv));

        MonHoc mh = mock(MonHoc.class);
        when(mh.getMaMon()).thenReturn("INT2204");
        when(mh.getSoTinChi()).thenReturn(3);
        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hk.getMaHK()).thenReturn("HK1_2024");
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getMaLHP()).thenReturn("INT2204_1");
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(lhp.getSiSoToiDa()).thenReturn(40);
        when(lhp.getMonHoc()).thenReturn(mh);
        when(lhp.getHocKy()).thenReturn(hk);

        when(lopHocPhanRepository.findByIdForUpdate("INT2204_1")).thenReturn(Optional.of(lhp));
        when(dangKyHocPhanRepository.findById(any(DangKyHocPhanId.class))).thenReturn(Optional.empty());
        when(dangKyHocPhanRepository.existsActiveCourseInTerm(any(), any(), any(), anyCollection(), any())).thenReturn(false);
        when(dieuKienMonHocRepository.findByMonHoc_MaMon("INT2204")).thenReturn(List.of());
        when(dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK("23020001", "HK1_2024")).thenReturn(List.of());
        when(academicPolicy.maxCreditsPerTerm()).thenReturn(25);
        when(lichGiangDayRepository.findByMaLHPIn(anyList())).thenReturn(List.of());
        when(dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(eq("INT2204_1"), anyCollection())).thenReturn(10L);

        dangKyHocPhanService.register("23020001", "INT2204_1");

        InOrder inOrder = inOrder(sinhVienRepository, lopHocPhanRepository, dangKyHocPhanRepository);
        inOrder.verify(sinhVienRepository).findByIdForUpdate("23020001");
        inOrder.verify(lopHocPhanRepository).findByIdForUpdate("INT2204_1");
        inOrder.verify(dangKyHocPhanRepository).saveAndFlush(any(DangKyHocPhan.class));
    }

    @Test
    @DisplayName("withdraw: Lớp học phần đã chốt danh sách sẽ không được phép rút lớp")
    void withdraw_sectionLocked_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getMaLHP()).thenReturn("INT2204_1");
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_CHOT_DANH_SACH);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dk.getLopHocPhan()).thenReturn(lhp);
        when(dangKyHocPhanRepository.findById(new DangKyHocPhanId("23020001", "INT2204_1"))).thenReturn(Optional.of(dk));

        assertThatThrownBy(() -> dangKyHocPhanService.withdraw("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.withdraw.locked");
    }

    @Test
    @DisplayName("withdraw: Lớp học phần đã bị hủy sẽ không được phép rút lớp")
    void withdraw_sectionCancelled_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getMaLHP()).thenReturn("INT2204_1");
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_HUY);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dk.getLopHocPhan()).thenReturn(lhp);
        when(dangKyHocPhanRepository.findById(new DangKyHocPhanId("23020001", "INT2204_1"))).thenReturn(Optional.of(dk));

        assertThatThrownBy(() -> dangKyHocPhanService.withdraw("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.withdraw.cancelled");
    }

    @Test
    @DisplayName("withdraw: Môn học đã có điểm quá trình hoặc điểm tổng sẽ không được phép rút lớp")
    void withdraw_alreadyHasProcessGrade_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dk.getLopHocPhan()).thenReturn(lhp);
        when(dangKyHocPhanRepository.findById(new DangKyHocPhanId("23020001", "INT2204_1"))).thenReturn(Optional.of(dk));

        KetQuaHocTap kq = mock(KetQuaHocTap.class);
        when(kq.getDiemQuaTrinh()).thenReturn(7.5);
        when(ketQuaHocTapRepository.findById_MaSVAndId_MaLHP("23020001", "INT2204_1")).thenReturn(Optional.of(kq));

        assertThatThrownBy(() -> dangKyHocPhanService.withdraw("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.withdrawGraded");
    }

    @Test
    @DisplayName("withdraw: Môn học đã có điểm cuối kỳ (chưa có điểm tổng) cũng không được phép rút lớp")
    void withdraw_hasFinalExamScore_throwsBusinessRuleException() {
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);

        DangKyHocPhan dk = mock(DangKyHocPhan.class);
        when(dk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dk.getLopHocPhan()).thenReturn(lhp);
        when(dangKyHocPhanRepository.findById(new DangKyHocPhanId("23020001", "INT2204_1"))).thenReturn(Optional.of(dk));

        KetQuaHocTap kq = mock(KetQuaHocTap.class);
        when(kq.getDiemQuaTrinh()).thenReturn(null);
        when(kq.getDiemGiuaKy()).thenReturn(null);
        when(kq.getDiemCuoiKy()).thenReturn(7.5);
        when(ketQuaHocTapRepository.findById_MaSVAndId_MaLHP("23020001", "INT2204_1")).thenReturn(Optional.of(kq));

        assertThatThrownBy(() -> dangKyHocPhanService.withdraw("23020001", "INT2204_1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("reg.withdrawGraded");
    }
}
