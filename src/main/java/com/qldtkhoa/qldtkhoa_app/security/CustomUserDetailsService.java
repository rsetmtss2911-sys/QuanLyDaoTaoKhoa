package com.qldtkhoa.qldtkhoa_app.security;

import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NhanSuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanSuRepository nhanSuRepository;
    private final SinhVienRepository sinhVienRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email));

        String role = taiKhoan.getVaiTro().getMaVaiTro();
        boolean enabled = Boolean.TRUE.equals(taiKhoan.getTrangThaiKichHoat());
        String displayName = email;
        String maNS = null;
        String maSV = null;
        boolean isAdvisor = false;

        if ("SINH_VIEN".equalsIgnoreCase(role)) {
            Optional<SinhVien> svOpt = sinhVienRepository.findByTaiKhoan_Email(email);
            if (svOpt.isPresent()) {
                SinhVien sv = svOpt.get();
                displayName = sv.getHoTen();
                maSV = sv.getMaSV();
            }
        } else {
            Optional<NhanSu> nsOpt = nhanSuRepository.findByTaiKhoan_Email(email);
            if (nsOpt.isPresent()) {
                NhanSu ns = nsOpt.get();
                displayName = ns.getHoTen();
                maNS = ns.getMaNS();
                if ("GIANG_VIEN".equalsIgnoreCase(role)) {
                    isAdvisor = lopHanhChinhRepository.existsByCoVan_MaNS(maNS);
                }
            }
        }

        return new AppUserPrincipal(
                taiKhoan.getEmail(),
                taiKhoan.getMatKhau(),
                role,
                enabled,
                displayName,
                maNS,
                maSV,
                isAdvisor
        );
    }
}
