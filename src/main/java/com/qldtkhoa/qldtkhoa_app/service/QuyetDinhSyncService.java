package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Dịch vụ đồng bộ các quyết định học vụ hẹn ngày tương lai khi tới ngày hiệu lực (GATE 3d).
 * <p>
 * Thiết kế idempotent không cần cột {@code DaApDung} trên bảng {@code QuyetDinhHocVu}
 * (tôn trọng Trigger {@code TRG_QuyetDinhHocVu_PreventModify}), xử lý từng sinh viên trong
 * giao dịch {@code PROPAGATION_REQUIRES_NEW} riêng để lỗi ở một hồ sơ không rollback cả lô.
 * </p>
 * <p>
 * [TIMEZONE NOTE - GATE 3d] Ngày hiện tại {@code today} được đọc từ {@link Clock} cấu hình theo
 * {@code app.academic.timezone} ({@code Asia/Ho_Chi_Minh}, UTC+7). Khi deploy SQL Server lên
 * môi trường UTC, giữ nguyên code Java này và chuyển comment phần {@code @Today} trong
 * {@code sql/Patch_v3_App_QLDTKhoa.sql} sang {@code AT TIME ZONE 'SE Asia Standard Time'}.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuyetDinhSyncService {

    private final QuyetDinhHocVuRepository quyetDinhHocVuRepository;
    private final SinhVienRepository sinhVienRepository;
    private final QuyetDinhHocVuService quyetDinhHocVuService;
    private final AuditService auditService;
    private final Clock clock;
    private final PlatformTransactionManager transactionManager;

    public int syncDueDecisions() {
        LocalDate today = clock != null ? LocalDate.now(clock) : LocalDate.now();
        List<String> candidateStudents = quyetDinhHocVuRepository.findStudentIdsWithDueDecisions(today);
        if (candidateStudents == null || candidateStudents.isEmpty()) {
            return 0;
        }

        TransactionTemplate requiresNewTx = new TransactionTemplate(transactionManager);
        requiresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int appliedCount = 0;
        for (String maSV : candidateStudents) {
            try {
                Boolean synced = requiresNewTx.execute(status -> syncSingleStudent(maSV, today));
                if (Boolean.TRUE.equals(synced)) {
                    appliedCount++;
                }
            } catch (Exception ex) {
                log.warn("Không thể đồng bộ quyết định đến hạn cho sinh viên {}: {}", maSV, ex.getMessage());
            }
        }
        return appliedCount;
    }

    boolean syncSingleStudent(String maSV, LocalDate today) {
        SinhVien sv = sinhVienRepository.findByIdForUpdate(maSV).orElse(null);
        if (sv == null) {
            return false;
        }
        List<QuyetDinhHocVu> effectiveList = quyetDinhHocVuRepository
                .findEffectiveForStudentOrderByLatest(maSV, today);
        if (effectiveList.isEmpty()) {
            return false;
        }

        QuyetDinhHocVu latest = effectiveList.get(0);
        String oldState = sv.getTrangThaiHienTai() != null ? sv.getTrangThaiHienTai().getLabel() : "—";
        boolean changed = quyetDinhHocVuService.applyDecisionEffects(sv, latest.getLoaiQuyetDinh());
        if (!changed) {
            return false;
        }

        sinhVienRepository.saveAndFlush(sv);
        auditService.logAs("SYSTEM", "APPLY_SCHEDULED_DECISION", "QuyetDinhHocVu", latest.getMaQuyetDinh(),
                oldState, sv.getTrangThaiHienTai().getLabel(),
                "Tự động áp dụng quyết định đến ngày hiệu lực " + latest.getNgayHieuLuc());

        if (latest.getLoaiQuyetDinh() == LoaiQuyetDinh.THOI_HOC && sv.getTaiKhoan() != null) {
            quyetDinhHocVuService.expireSessionsAfterCommit(sv.getTaiKhoan().getEmail());
        }
        return true;
    }
}
