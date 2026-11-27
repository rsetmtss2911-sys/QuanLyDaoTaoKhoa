package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.NhatKyHeThong;
import com.qldtkhoa.qldtkhoa_app.repository.NhatKyHeThongRepository;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final NhatKyHeThongRepository nhatKyHeThongRepository;
    private final Clock clock;

    @Transactional
    public void log(String hanhDong,
                    String doiTuong,
                    String maBanGhi,
                    String giaTriCu,
                    String giaTriMoi,
                    String lyDo) {
        logAs(resolveCurrentActor(), hanhDong, doiTuong, maBanGhi, giaTriCu, giaTriMoi, lyDo);
    }

    @Transactional
    public void logAs(String actor,
                      String hanhDong,
                      String doiTuong,
                      String maBanGhi,
                      String giaTriCu,
                      String giaTriMoi,
                      String lyDo) {
        String safeActor = (actor == null || actor.isBlank()) ? "SYSTEM" : truncate(actor.trim(), 100);
        LocalDateTime now = clock != null ? LocalDateTime.now(clock) : LocalDateTime.now();
        NhatKyHeThong entry = new NhatKyHeThong(
                now,
                safeActor,
                truncate(hanhDong, 50),
                truncate(doiTuong, 50),
                truncate(maBanGhi, 100),
                giaTriCu,
                giaTriMoi,
                truncate(lyDo, 255)
        );
        nhatKyHeThongRepository.save(entry);
    }

    public Page<NhatKyRow> search(String q, String doiTuong, String hanhDong, Pageable pageable) {
        return nhatKyHeThongRepository.search(
                LikeUtils.escape(q),
                blankToNull(doiTuong),
                blankToNull(hanhDong),
                pageable
        );
    }

    public List<NhatKyRow> findForExport(String q, String doiTuong, String hanhDong) {
        return nhatKyHeThongRepository.findForExport(
                LikeUtils.escape(q),
                blankToNull(doiTuong),
                blankToNull(hanhDong),
                PageRequest.of(0, 5000)
        );
    }

    public List<String> findDistinctDoiTuong() {
        return nhatKyHeThongRepository.findDistinctDoiTuong();
    }

    public List<String> findDistinctHanhDong() {
        return nhatKyHeThongRepository.findDistinctHanhDong();
    }

    private String resolveCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
