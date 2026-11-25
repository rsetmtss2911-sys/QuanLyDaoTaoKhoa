package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.MonHocForm;
import com.qldtkhoa.qldtkhoa_app.dto.MonHocRow;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.DieuKienMonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.id.DieuKienMonHocId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DieuKienMonHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonHocService {

    private final MonHocRepository monHocRepository;
    private final BoMonRepository boMonRepository;
    private final DieuKienMonHocRepository dieuKienMonHocRepository;
    private final ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public Page<MonHocRow> search(String q, String maBoMon, Pageable pageable) {
        return monHocRepository.search(q == null ? "" : q.trim(), maBoMon, pageable)
                .map(this::toRow);
    }

    public List<MonHoc> findAll() {
        return monHocRepository.findAllByOrderByTenMonAsc();
    }

    public MonHoc getOrThrow(String maMon) {
        return monHocRepository.findById(maMon)
                .orElseThrow(() -> new ResourceNotFoundException("monhoc.notFound", maMon));
    }

    public MonHoc getWithDetailsOrThrow(String maMon) {
        return monHocRepository.findByIdWithDetails(maMon)
                .orElseThrow(() -> new ResourceNotFoundException("monhoc.notFound", maMon));
    }

    public MonHocForm getForm(String maMon) {
        MonHoc mh = getOrThrow(maMon);
        MonHocForm form = new MonHocForm();
        form.setMaMon(mh.getMaMon());
        form.setTenMon(mh.getTenMon());
        form.setSoTinChi(mh.getSoTinChi());
        form.setTinChiLT(mh.getTinChiLT());
        form.setTinChiTH(mh.getTinChiTH());
        form.setTrongSoQT(mh.getTrongSoQT());
        form.setTrongSoGK(mh.getTrongSoGK());
        form.setTrongSoCK(mh.getTrongSoCK());
        form.setMaBoMon(mh.getBoMon().getMaBoMon());
        return form;
    }

    public List<DieuKienMonHoc> findPrerequisites(String maMon) {
        return dieuKienMonHocRepository.findByMonHoc_MaMon(maMon);
    }

    @Transactional
    public void create(MonHocForm form) {
        String ma = form.getMaMon().trim();
        if (monHocRepository.existsById(ma)) {
            throw new DuplicateResourceException("monhoc.maMon.duplicate", ma);
        }

        validateCreditsAndWeights(form);

        BoMon bm = boMonRepository.findById(form.getMaBoMon())
                .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", form.getMaBoMon()));

        MonHoc mh = new MonHoc(
                ma,
                form.getTenMon().trim(),
                form.getSoTinChi(),
                form.getTinChiLT(),
                form.getTinChiTH(),
                form.getTrongSoQT(),
                form.getTrongSoGK(),
                form.getTrongSoCK(),
                bm
        );
        monHocRepository.save(mh);
    }

    @Transactional
    public void update(String maMon, MonHocForm form) {
        MonHoc mh = getOrThrow(maMon);
        validateCreditsAndWeights(form);

        BoMon bm = boMonRepository.findById(form.getMaBoMon())
                .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", form.getMaBoMon()));

        mh.setTenMon(form.getTenMon().trim());
        mh.setSoTinChi(form.getSoTinChi());
        mh.setTinChiLT(form.getTinChiLT());
        mh.setTinChiTH(form.getTinChiTH());
        mh.setTrongSoQT(form.getTrongSoQT());
        mh.setTrongSoGK(form.getTrongSoGK());
        mh.setTrongSoCK(form.getTrongSoCK());
        mh.setBoMon(bm);
    }

    @Transactional
    public void addPrerequisite(String maMon, String maMonTienQuyet) {
        if (maMon.equalsIgnoreCase(maMonTienQuyet)) {
            throw new BusinessRuleException("Môn học không thể là môn tiên quyết của chính nó!");
        }

        MonHoc mh = getOrThrow(maMon);
        MonHoc mhTienQuyet = getOrThrow(maMonTienQuyet);

        if (dieuKienMonHocRepository.existsByMonHoc_MaMonAndMonTienQuyet_MaMon(maMon, maMonTienQuyet)) {
            throw new BusinessRuleException("Điều kiện tiên quyết này đã được thiết lập trước đó!");
        }

        // Kiểm tra chu trình phụ thuộc (Cycle Detection qua BFS)
        // Nếu đã có đường đi từ maMonTienQuyet -> maMon thì việc thêm maMon -> maMonTienQuyet sẽ tạo chu trình!
        if (hasPath(maMonTienQuyet, maMon)) {
            throw new BusinessRuleException("Vi phạm điều kiện tiên quyết: Tạo chu trình phụ thuộc giữa các môn học!");
        }

        DieuKienMonHoc dk = new DieuKienMonHoc(mh, mhTienQuyet);
        dieuKienMonHocRepository.save(dk);
    }

    @Transactional
    public void removePrerequisite(String maMon, String maMonTienQuyet) {
        DieuKienMonHocId id = new DieuKienMonHocId(maMon, maMonTienQuyet);
        if (dieuKienMonHocRepository.existsById(id)) {
            dieuKienMonHocRepository.deleteById(id);
        }
    }

    private boolean hasPath(String start, String target) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equalsIgnoreCase(target)) {
                return true;
            }
            List<DieuKienMonHoc> edges = dieuKienMonHocRepository.findByMonHoc_MaMon(current);
            for (DieuKienMonHoc edge : edges) {
                String next = edge.getMonTienQuyet().getMaMon();
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return false;
    }

    @Transactional
    public void delete(String maMon) {
        getOrThrow(maMon);
        if (chuongTrinhDaoTaoRepository.existsByMonHoc_MaMon(maMon)) {
            throw new BusinessRuleException("monhoc.delete.hasCurriculum", "Môn học đang có trong chương trình đào tạo, không thể xóa!");
        }
        if (lopHocPhanRepository.existsByMonHoc_MaMon(maMon)) {
            throw new BusinessRuleException("monhoc.delete.hasSections", "Môn học đã có lớp học phần được mở, không thể xóa!");
        }

        // Xóa các điều kiện tiên quyết liên quan
        List<DieuKienMonHoc> prereqs = dieuKienMonHocRepository.findByMonHoc_MaMon(maMon);
        dieuKienMonHocRepository.deleteAll(prereqs);

        List<DieuKienMonHoc> reversePrereqs = dieuKienMonHocRepository.findByMonTienQuyet_MaMon(maMon);
        dieuKienMonHocRepository.deleteAll(reversePrereqs);

        monHocRepository.deleteById(maMon);
    }

    private void validateCreditsAndWeights(MonHocForm form) {
        if (form.getTinChiLT() + form.getTinChiTH() != form.getSoTinChi()) {
            throw new BusinessRuleException("Tổng số tín chỉ lý thuyết (" + form.getTinChiLT() + ") và thực hành (" + form.getTinChiTH() + ") phải bằng tổng số tín chỉ (" + form.getSoTinChi() + ")!");
        }

        double sumWeights = form.getTrongSoQT() + form.getTrongSoGK() + form.getTrongSoCK();
        if (Math.abs(sumWeights - 1.0) > 0.01) {
            throw new BusinessRuleException("Tổng trọng số đánh giá (QT + GK + CK) phải bằng 1.0 (100%)! Hiện tại: " + Math.round(sumWeights * 100) + "%");
        }
    }

    public MonHocRow toRow(MonHoc mh) {
        int soLuongTienQuyet = (int) dieuKienMonHocRepository.countByMonHoc_MaMon(mh.getMaMon());
        return new MonHocRow(
                mh.getMaMon(),
                mh.getTenMon(),
                mh.getSoTinChi(),
                mh.getTinChiLT(),
                mh.getTinChiTH(),
                mh.getTrongSoQT(),
                mh.getTrongSoGK(),
                mh.getTrongSoCK(),
                mh.getBoMon().getMaBoMon(),
                mh.getBoMon().getTenBoMon(),
                soLuongTienQuyet
        );
    }
}
