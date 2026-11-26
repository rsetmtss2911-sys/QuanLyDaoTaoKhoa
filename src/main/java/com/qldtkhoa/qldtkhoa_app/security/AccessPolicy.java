package com.qldtkhoa.qldtkhoa_app.security;

import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Chính sách kiểm tra quyền sở hữu dữ liệu (Record Ownership) dùng chung cho Controller và Service.
 */
@Component("accessPolicy")
@RequiredArgsConstructor
public class AccessPolicy {

    private final LopHocPhanRepository lopHocPhanRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final SinhVienRepository sinhVienRepository;

    public boolean teachesSection(Authentication auth, String maLHP) {
        if (!(auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) || principal.getMaNS() == null) {
            return false;
        }
        return lopHocPhanRepository.findByIdWithDetails(maLHP)
                .map(l -> l.getGiangVien() != null && l.getGiangVien().getMaNS().equalsIgnoreCase(principal.getMaNS()))
                .orElse(false);
    }

    public boolean advisesClass(Authentication auth, String maLopHC) {
        if (!(auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) || principal.getMaNS() == null) {
            return false;
        }
        return lopHanhChinhRepository.findByIdWithDetails(maLopHC)
                .map(l -> l.getCoVan() != null && l.getCoVan().getMaNS().equalsIgnoreCase(principal.getMaNS()))
                .orElse(false);
    }

    public boolean advisesStudent(Authentication auth, String maSV) {
        if (!(auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) || principal.getMaNS() == null) {
            return false;
        }
        return sinhVienRepository.findByIdWithDetails(maSV)
                .map(sv -> sv.getLopHanhChinh() != null
                        && sv.getLopHanhChinh().getCoVan() != null
                        && sv.getLopHanhChinh().getCoVan().getMaNS().equalsIgnoreCase(principal.getMaNS()))
                .orElse(false);
    }

    public boolean isSelf(Authentication auth, String maSV) {
        if (!(auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) || principal.getMaSV() == null) {
            return false;
        }
        return principal.getMaSV().equalsIgnoreCase(maSV);
    }
}
