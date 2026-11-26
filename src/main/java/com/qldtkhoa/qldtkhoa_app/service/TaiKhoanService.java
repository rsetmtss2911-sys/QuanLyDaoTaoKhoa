package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.ChangePasswordForm;
import com.qldtkhoa.qldtkhoa_app.dto.TaiKhoanRow;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionInvalidationHelper sessionInvalidationHelper;
    private final AuditService auditService;

    public Page<TaiKhoan> search(String q, String maVaiTro, Pageable pageable) {
        return taiKhoanRepository.search(LikeUtils.escape(q), maVaiTro, pageable);
    }

    public Page<TaiKhoanRow> searchRows(String q, String maVaiTro, Pageable pageable) {
        return taiKhoanRepository.search(LikeUtils.escape(q), maVaiTro, pageable)
                .map(tk -> new TaiKhoanRow(
                        tk.getEmail(),
                        tk.getVaiTro().getMaVaiTro(),
                        tk.getVaiTro().getTenVaiTro(),
                        tk.getTrangThaiKichHoat()
                ));
    }

    public TaiKhoan getByEmail(String email) {
        return taiKhoanRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound", email));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordForm form) {
        changePassword(email, form, null);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordForm form, String keepSessionId) {
        TaiKhoan tk = getByEmail(email);

        if (!passwordEncoder.matches(form.getMatKhauCu(), tk.getMatKhau())) {
            throw new BusinessRuleException("auth.password.oldMismatch");
        }
        if (passwordEncoder.matches(form.getMatKhauMoi(), tk.getMatKhau())) {
            throw new BusinessRuleException("auth.password.sameAsOld");
        }
        if (!form.getMatKhauMoi().equals(form.getXacNhanMatKhau())) {
            throw new BusinessRuleException("auth.password.confirmMismatch");
        }

        tk.setMatKhau(passwordEncoder.encode(form.getMatKhauMoi()));
        taiKhoanRepository.saveAndFlush(tk);
        sessionInvalidationHelper.expireOtherSessions(email, keepSessionId);
    }

    @Transactional
    public boolean toggleLock(String targetEmail, String currentAdminEmail) {
        String cleanTarget = targetEmail == null ? "" : targetEmail.trim();
        if (currentAdminEmail != null && cleanTarget.equalsIgnoreCase(currentAdminEmail.trim())) {
            throw new BusinessRuleException("taikhoan.error.selfLock");
        }

        TaiKhoan tk = getByEmail(cleanTarget);
        boolean currentlyActive = Boolean.TRUE.equals(tk.getTrangThaiKichHoat());
        if (currentlyActive
                && tk.getVaiTro() != null
                && "ADMIN".equalsIgnoreCase(tk.getVaiTro().getMaVaiTro())
                && taiKhoanRepository.countByVaiTro_MaVaiTroAndTrangThaiKichHoat("ADMIN", true) <= 1) {
            throw new BusinessRuleException("taikhoan.error.lastAdmin");
        }

        tk.setTrangThaiKichHoat(!currentlyActive);
        taiKhoanRepository.saveAndFlush(tk);
        if (currentlyActive) {
            sessionInvalidationHelper.expireUserSessions(cleanTarget);
        }
        if (auditService != null) {
            auditService.log(currentlyActive ? "LOCK_ACCOUNT" : "UNLOCK_ACCOUNT",
                    "TaiKhoan", cleanTarget,
                    currentlyActive ? "Hoạt động" : "Đã khóa",
                    currentlyActive ? "Đã khóa" : "Hoạt động",
                    "Quản trị viên thao tác trạng thái tài khoản");
        }
        return currentlyActive;
    }

    @Transactional
    public void setLockStatus(String email, boolean locked) {
        TaiKhoan tk = getByEmail(email);
        if (locked
                && tk.getVaiTro() != null
                && "ADMIN".equalsIgnoreCase(tk.getVaiTro().getMaVaiTro())
                && Boolean.TRUE.equals(tk.getTrangThaiKichHoat())
                && taiKhoanRepository.countByVaiTro_MaVaiTroAndTrangThaiKichHoat("ADMIN", true) <= 1) {
            throw new BusinessRuleException("taikhoan.error.lastAdmin");
        }
        tk.setTrangThaiKichHoat(!locked);
        taiKhoanRepository.saveAndFlush(tk);
        if (locked) {
            sessionInvalidationHelper.expireUserSessions(email);
        }
    }

    @Transactional
    public void resetPassword(String email, String defaultPassword) {
        TaiKhoan tk = getByEmail(email);
        tk.setMatKhau(passwordEncoder.encode(defaultPassword));
        taiKhoanRepository.saveAndFlush(tk);
        sessionInvalidationHelper.expireUserSessions(email);
        if (auditService != null) {
            auditService.log("RESET_PASSWORD", "TaiKhoan", email, null, "Đặt lại mật khẩu mặc định",
                    "Quản trị viên đặt lại mật khẩu");
        }
    }
}
