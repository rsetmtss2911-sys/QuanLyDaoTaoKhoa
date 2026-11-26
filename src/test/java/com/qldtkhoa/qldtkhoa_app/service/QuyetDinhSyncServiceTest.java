package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuyetDinhSyncServiceTest {

    @Mock
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private QuyetDinhHocVuService quyetDinhHocVuService;

    @Mock
    private AuditService auditService;

    @Mock
    private PlatformTransactionManager transactionManager;

    private Clock fixedClock;
    private QuyetDinhSyncService syncService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-03-01T00:05:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        syncService = new QuyetDinhSyncService(
                quyetDinhHocVuRepository,
                sinhVienRepository,
                quyetDinhHocVuService,
                auditService,
                fixedClock,
                transactionManager
        );
    }

    @Test
    @DisplayName("syncDueDecisions: Áp dụng quyết định THOI_HOC đến hạn, idempotent ở lần chạy thứ 2")
    void syncDueDecisions_dueThoiHocAndIdempotentRerun_appliesOnceAndLogsAudit() {
        LocalDate today = LocalDate.of(2026, 3, 1);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        when(quyetDinhHocVuRepository.findStudentIdsWithDueDecisions(today))
                .thenReturn(List.of("SV01"));

        TaiKhoan tk = mock(TaiKhoan.class);
        when(tk.getEmail()).thenReturn("sv01@student.edu.vn");
        SinhVien sv = mock(SinhVien.class);
        when(sv.getTrangThaiHienTai())
                .thenReturn(TrangThaiSinhVien.DANG_HOC)
                .thenReturn(TrangThaiSinhVien.THOI_HOC);
        when(sv.getTaiKhoan()).thenReturn(tk);
        when(sinhVienRepository.findByIdForUpdate("SV01")).thenReturn(Optional.of(sv));

        QuyetDinhHocVu qd = mock(QuyetDinhHocVu.class);
        when(qd.getMaQuyetDinh()).thenReturn("QD_FUTURE_01");
        when(qd.getLoaiQuyetDinh()).thenReturn(LoaiQuyetDinh.THOI_HOC);
        when(qd.getNgayHieuLuc()).thenReturn(today);
        when(quyetDinhHocVuRepository.findEffectiveForStudentOrderByLatest("SV01", today))
                .thenReturn(List.of(qd));

        when(quyetDinhHocVuService.applyDecisionEffects(sv, LoaiQuyetDinh.THOI_HOC))
                .thenReturn(true)
                .thenReturn(false);

        int firstRun = syncService.syncDueDecisions();
        int secondRun = syncService.syncDueDecisions();

        assertThat(firstRun).isEqualTo(1);
        assertThat(secondRun).isEqualTo(0);

        verify(quyetDinhHocVuService, times(1)).expireSessionsAfterCommit("sv01@student.edu.vn");
        verify(auditService, times(1)).logAs(eq("SYSTEM"), eq("APPLY_SCHEDULED_DECISION"),
                eq("QuyetDinhHocVu"), eq("QD_FUTURE_01"), any(), any(), any());
    }

    @Test
    @DisplayName("syncDueDecisions: Lỗi ở sinh viên đầu tiên không làm gián đoạn đồng bộ sinh viên kế tiếp (REQUIRES_NEW)")
    void syncDueDecisions_singleStudentFailure_continuesRemainingStudents() {
        LocalDate today = LocalDate.of(2026, 3, 1);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        when(quyetDinhHocVuRepository.findStudentIdsWithDueDecisions(today))
                .thenReturn(List.of("SV_ERR", "SV_OK"));

        when(sinhVienRepository.findByIdForUpdate("SV_ERR")).thenThrow(new RuntimeException("Lock timeout"));

        SinhVien svOk = mock(SinhVien.class);
        when(svOk.getTrangThaiHienTai())
                .thenReturn(TrangThaiSinhVien.DANG_HOC)
                .thenReturn(TrangThaiSinhVien.BAO_LUU);
        when(sinhVienRepository.findByIdForUpdate("SV_OK")).thenReturn(Optional.of(svOk));

        QuyetDinhHocVu qdOk = mock(QuyetDinhHocVu.class);
        when(qdOk.getMaQuyetDinh()).thenReturn("QD_OK");
        when(qdOk.getLoaiQuyetDinh()).thenReturn(LoaiQuyetDinh.BAO_LUU);
        when(qdOk.getNgayHieuLuc()).thenReturn(today);
        when(quyetDinhHocVuRepository.findEffectiveForStudentOrderByLatest("SV_OK", today))
                .thenReturn(List.of(qdOk));
        when(quyetDinhHocVuService.applyDecisionEffects(svOk, LoaiQuyetDinh.BAO_LUU)).thenReturn(true);

        int applied = syncService.syncDueDecisions();

        assertThat(applied).isEqualTo(1);
        verify(sinhVienRepository).saveAndFlush(svOk);
    }
}
