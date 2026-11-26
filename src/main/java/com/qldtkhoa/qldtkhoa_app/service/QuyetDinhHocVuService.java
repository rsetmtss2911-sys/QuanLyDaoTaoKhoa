package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhForm;
import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuyetDinhHocVuService {

    private static final List<TrangThaiDangKy> ACTIVE_REG_STATUSES =
            List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI);

    private final QuyetDinhHocVuRepository quyetDinhHocVuRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final SessionInvalidationHelper sessionInvalidationHelper;
    private final AuditService auditService;
    private final Clock clock;

    public Page<QuyetDinhRow> search(String q, String maSV, Pageable pageable) {
        LocalDate today = currentDate();
        return quyetDinhHocVuRepository.search(LikeUtils.escape(q), maSV, pageable)
                .map(qd -> toRow(qd, today));
    }

    /**
     * Ban hành quyết định học vụ mới (hiệu lực ngay hoặc hẹn ngày tương lai tối đa +1 năm).
     *
     * @return {@code true} nếu quyết định hẹn ngày tương lai ({@code pendingEffective}),
     *         {@code false} nếu có hiệu lực và đã áp dụng ngay.
     */
    @Transactional
    public boolean create(QuyetDinhForm form) {
        String ma = form.getMaQuyetDinh().trim();
        if (quyetDinhHocVuRepository.existsById(ma)) {
            throw new DuplicateResourceException("quyetdinh.maQuyetDinh.duplicate", ma);
        }

        String lyDo = form.getLyDo() != null ? form.getLyDo().trim() : "";
        if (lyDo.isEmpty()) {
            throw new BusinessRuleException("quyetdinh.lyDo.required");
        }

        String maSV = form.getMaSV().trim();
        SinhVien sv = sinhVienRepository.findByIdForUpdate(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));

        LocalDate today = currentDate();
        LocalDate ngayHieuLuc = form.getNgayHieuLuc();
        if (ngayHieuLuc == null) {
            throw new BusinessRuleException("quyetdinh.ngayHieuLuc.required");
        }
        if (ngayHieuLuc.isAfter(today.plusYears(1))) {
            throw new BusinessRuleException("decision.error.futureTooFar");
        }

        Optional<QuyetDinhHocVu> latestOpt = quyetDinhHocVuRepository
                .findTopBySinhVien_MaSVOrderByNgayHieuLucDescMaQuyetDinhDesc(maSV);
        if (latestOpt.isPresent()) {
            LocalDate latestDate = latestOpt.get().getNgayHieuLuc();
            if (latestDate != null && !ngayHieuLuc.isAfter(latestDate)) {
                throw new BusinessRuleException("decision.error.notAfterLatest", latestDate.toString());
            }
        }

        TrangThaiSinhVien projectedState = latestOpt
                .map(qd -> targetStateOf(qd.getLoaiQuyetDinh()))
                .orElse(sv.getTrangThaiHienTai());

        LoaiQuyetDinh lqd = form.getLoaiQuyetDinh();
        validateTransition(projectedState, lqd);

        boolean futureEffective = ngayHieuLuc.isAfter(today);
        String oldStateLabel = sv.getTrangThaiHienTai() != null ? sv.getTrangThaiHienTai().getLabel() : "—";

        if (!futureEffective) {
            applyDecisionEffects(sv, lqd);
            sinhVienRepository.save(sv);
        }

        QuyetDinhHocVu qd = new QuyetDinhHocVu(ma, sv, lqd, ngayHieuLuc, lyDo);
        quyetDinhHocVuRepository.saveAndFlush(qd);

        if (futureEffective) {
            auditService.log("SCHEDULE_DECISION", "QuyetDinhHocVu", ma,
                    oldStateLabel, lqd.getLabel() + " (hiệu lực " + ngayHieuLuc + ")", lyDo);
            return true;
        }

        auditService.log("ISSUE_DECISION", "QuyetDinhHocVu", ma,
                oldStateLabel, targetStateOf(lqd).getLabel(), lyDo);

        if (lqd == LoaiQuyetDinh.THOI_HOC && sv.getTaiKhoan() != null && sessionInvalidationHelper != null) {
            expireSessionsAfterCommit(sv.getTaiKhoan().getEmail());
        }
        return false;
    }

    public List<QuyetDinhRow> findBySinhVien(String maSV) {
        LocalDate today = currentDate();
        return quyetDinhHocVuRepository.findBySinhVien_MaSVOrderByNgayHieuLucDesc(maSV).stream()
                .map(qd -> toRow(qd, today))
                .toList();
    }

    @Transactional
    public void delete(String maQuyetDinh) {
        throw new BusinessRuleException("decision.error.immutable");
    }

    public static TrangThaiSinhVien targetStateOf(LoaiQuyetDinh lqd) {
        return switch (lqd) {
            case BAO_LUU -> TrangThaiSinhVien.BAO_LUU;
            case DINH_CHI -> TrangThaiSinhVien.DINH_CHI;
            case THOI_HOC -> TrangThaiSinhVien.THOI_HOC;
            case TRO_LAI_HOC -> TrangThaiSinhVien.DANG_HOC;
            case TOT_NGHIEP -> TrangThaiSinhVien.DA_TOT_NGHIEP;
        };
    }

    private void validateTransition(TrangThaiSinhVien currentState, LoaiQuyetDinh decisionType) {
        if (currentState == TrangThaiSinhVien.THOI_HOC || currentState == TrangThaiSinhVien.DA_TOT_NGHIEP) {
            throw new BusinessRuleException("decision.error.terminalState", currentState.getLabel());
        }
        boolean allowed = switch (decisionType) {
            case BAO_LUU, DINH_CHI, TOT_NGHIEP -> currentState == TrangThaiSinhVien.DANG_HOC;
            case THOI_HOC -> currentState == TrangThaiSinhVien.DANG_HOC
                    || currentState == TrangThaiSinhVien.BAO_LUU
                    || currentState == TrangThaiSinhVien.DINH_CHI;
            case TRO_LAI_HOC -> currentState == TrangThaiSinhVien.BAO_LUU
                    || currentState == TrangThaiSinhVien.DINH_CHI;
        };
        if (!allowed) {
            throw new BusinessRuleException("decision.error.invalidTransition",
                    decisionType.getLabel(), currentState.getLabel());
        }
    }

    /**
     * Áp dụng hiệu ứng quyết định lên {@code SinhVien}, {@code TaiKhoan} và các lớp {@code Chờ mở}.
     * Trả về {@code true} nếu có ít nhất một thay đổi thực tế so với trạng thái hiện có (idempotent).
     */
    boolean applyDecisionEffects(SinhVien sv, LoaiQuyetDinh lqd) {
        boolean changed = false;
        TrangThaiSinhVien targetState = targetStateOf(lqd);
        if (sv.getTrangThaiHienTai() != targetState) {
            sv.setTrangThaiHienTai(targetState);
            changed = true;
        }
        if (lqd == LoaiQuyetDinh.THOI_HOC && sv.getTaiKhoan() != null
                && !Boolean.FALSE.equals(sv.getTaiKhoan().getTrangThaiKichHoat())) {
            sv.getTaiKhoan().setTrangThaiKichHoat(false);
            changed = true;
        } else if (lqd == LoaiQuyetDinh.TRO_LAI_HOC && sv.getTaiKhoan() != null
                && !Boolean.TRUE.equals(sv.getTaiKhoan().getTrangThaiKichHoat())) {
            sv.getTaiKhoan().setTrangThaiKichHoat(true);
            changed = true;
        }

        if (lqd == LoaiQuyetDinh.BAO_LUU || lqd == LoaiQuyetDinh.DINH_CHI
                || lqd == LoaiQuyetDinh.THOI_HOC || lqd == LoaiQuyetDinh.TOT_NGHIEP) {
            List<DangKyHocPhan> openRegs = dangKyHocPhanRepository.findActiveInSectionsWithStatus(
                    sv.getMaSV(), TrangThaiLop.CHO_MO, ACTIVE_REG_STATUSES);
            if (!openRegs.isEmpty()) {
                for (DangKyHocPhan dk : openRegs) {
                    dk.setTrangThaiDangKy(TrangThaiDangKy.DA_RUT_LOP);
                }
                dangKyHocPhanRepository.saveAllAndFlush(openRegs);
                changed = true;
            }
        }
        return changed;
    }

    void expireSessionsAfterCommit(String email) {
        if (email == null || sessionInvalidationHelper == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sessionInvalidationHelper.expireUserSessions(email);
                }
            });
        } else {
            sessionInvalidationHelper.expireUserSessions(email);
        }
    }

    private LocalDate currentDate() {
        return clock != null ? LocalDate.now(clock) : LocalDate.now();
    }

    private QuyetDinhRow toRow(QuyetDinhHocVu qd, LocalDate today) {
        boolean pending = qd.getNgayHieuLuc() != null && qd.getNgayHieuLuc().isAfter(today);
        String maLopHC = qd.getSinhVien().getLopHanhChinh() != null
                ? qd.getSinhVien().getLopHanhChinh().getMaLopHC()
                : "—";
        return new QuyetDinhRow(
                qd.getMaQuyetDinh(),
                qd.getSinhVien().getMaSV(),
                qd.getSinhVien().getHoTen(),
                maLopHC,
                qd.getLoaiQuyetDinh(),
                qd.getNgayHieuLuc(),
                qd.getLyDo(),
                pending
        );
    }
}
