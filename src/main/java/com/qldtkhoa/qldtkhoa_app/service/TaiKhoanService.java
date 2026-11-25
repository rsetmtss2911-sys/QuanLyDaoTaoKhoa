package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.ChangePasswordForm;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
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

    public Page<TaiKhoan> search(String q, String maVaiTro, Pageable pageable) {
        return taiKhoanRepository.search(q == null ? "" : q.trim(), maVaiTro, pageable);
    }

    public Page<com.qldtkhoa.qldtkhoa_app.dto.TaiKhoanRow> searchRows(String q, String maVaiTro, Pageable pageable) {
        return taiKhoanRepository.search(q == null ? "" : q.trim(), maVaiTro, pageable)
                .map(tk -> new com.qldtkhoa.qldtkhoa_app.dto.TaiKhoanRow(
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
        TaiKhoan tk = getByEmail(email);

        if (!passwordEncoder.matches(form.getMatKhauCu(), tk.getMatKhau())) {
            throw new BusinessRuleException("auth.password.oldMismatch");
        }
        if (!form.getMatKhauMoi().equals(form.getXacNhanMatKhau())) {
            throw new BusinessRuleException("auth.password.confirmMismatch");
        }

        tk.setMatKhau(passwordEncoder.encode(form.getMatKhauMoi()));
    }

    @Transactional
    public void setLockStatus(String email, boolean locked) {
        TaiKhoan tk = getByEmail(email);
        tk.setTrangThaiKichHoat(!locked);
    }

    @Transactional
    public void resetPassword(String email, String defaultPassword) {
        TaiKhoan tk = getByEmail(email);
        tk.setMatKhau(passwordEncoder.encode(defaultPassword));
    }
}
