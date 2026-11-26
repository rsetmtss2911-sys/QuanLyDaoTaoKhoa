package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.HocLucSummary;
import com.qldtkhoa.qldtkhoa_app.dto.TermTranscriptSummary;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GpaRepeatPolicy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Single Source of Truth tính GPA từng học kỳ, CPA tích lũy (theo Thông tư 08/2021/TT-BGDĐT),
 * quy đổi Cấm thi (0.0/F), loại trừ môn không tính GPA ({@code TinhVaoGPA = false})
 * và đánh giá cảnh báo học vụ.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HocLucService {

    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
    private final AcademicPolicy academicPolicy;

    public BangDiemSinhVienResponse evaluateStudent(String maSV) {
        List<BangDiemSinhVienView> views = bangDiemViewRepository
                .findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(maSV);
        return evaluateTranscript(views);
    }

    public HocLucSummary evaluateSummary(List<BangDiemSinhVienView> views) {
        EvaluationResult result = compute(views);
        return new HocLucSummary(
                result.tongTCTichLuy(),
                result.cpa4(),
                result.xepLoai(),
                result.soMonTruot(),
                result.coCamThi(),
                result.canhBaoHocVu(),
                result.lyDoCanhBao(),
                result.termSummaries()
        );
    }

    public BangDiemSinhVienResponse evaluateTranscript(List<BangDiemSinhVienView> views) {
        EvaluationResult result = compute(views);
        return new BangDiemSinhVienResponse(
                result.byTerm(),
                result.tongTCTichLuy(),
                result.cpa4(),
                result.xepLoai(),
                result.termSummaries(),
                result.canhBaoHocVu(),
                result.lyDoCanhBao()
        );
    }

    private EvaluationResult compute(List<BangDiemSinhVienView> rawViews) {
        List<BangDiemSinhVienView> activeViews = (rawViews == null ? List.<BangDiemSinhVienView>of() : rawViews).stream()
                .filter(v -> v.getTrangThaiLop() != TrangThaiLop.DA_HUY)
                .toList();

        Map<String, List<BangDiemSinhVienView>> byTerm = activeViews.stream()
                .collect(Collectors.groupingBy(
                        this::formatTermLabel,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        double passScore = academicPolicy.passScore();
        GpaRepeatPolicy repeatPolicy = academicPolicy.gpaRepeatPolicy();

        // 1. GPA từng học kỳ (bao gồm cả điểm F và Cấm thi; loại trừ TinhVaoGPA == false)
        Map<String, TermTranscriptSummary> termSummaries = new LinkedHashMap<>();
        List<TermEval> evaluatedTermsAsc = new ArrayList<>();

        for (Map.Entry<String, List<BangDiemSinhVienView>> entry : byTerm.entrySet()) {
            int gpaCredits = 0;
            double gpaWeighted = 0.0;
            int passedCredits = 0;
            int gradedAnyCredits = 0;
            int failedAnyCredits = 0;
            Integer namHoc = null;
            String maHK = null;

            for (BangDiemSinhVienView v : entry.getValue()) {
                if (namHoc == null) namHoc = v.getNamHoc();
                if (maHK == null) maHK = v.getMaHK();

                Double effTong = effectiveTotal(v);
                if (effTong == null) {
                    continue;
                }
                int tc = v.getSoTinChi() != null ? v.getSoTinChi() : 0;
                gradedAnyCredits += tc;
                if (effTong >= passScore) {
                    passedCredits += tc;
                } else {
                    failedAnyCredits += tc;
                }

                if (!Boolean.FALSE.equals(v.getTinhVaoGPA())) {
                    Double eff4 = effectiveGpa4(v, effTong);
                    if (eff4 != null && tc > 0) {
                        gpaCredits += tc;
                        gpaWeighted += eff4 * tc;
                    }
                }
            }

            Double termGpa = round2(gpaCredits > 0 ? gpaWeighted / gpaCredits : null);
            termSummaries.put(entry.getKey(), new TermTranscriptSummary(
                    entry.getKey(), gpaCredits, passedCredits, termGpa
            ));
            if (gradedAnyCredits > 0) {
                evaluatedTermsAsc.add(new TermEval(
                        entry.getKey(), namHoc != null ? namHoc : 0, maHK != null ? maHK : "",
                        termGpa, gradedAnyCredits, failedAnyCredits
                ));
            }
        }

        // 2. CPA tích lũy & Tín chỉ tích lũy theo từng MaMon (chọn 1 lần đại diện theo repeatPolicy)
        Map<String, BangDiemSinhVienView> selectedBySubject = new LinkedHashMap<>();
        boolean coCamThi = false;
        for (BangDiemSinhVienView v : activeViews) {
            if (v.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI) {
                coCamThi = true;
            }
            if (effectiveTotal(v) == null || v.getMaMon() == null) {
                continue;
            }
            selectedBySubject.merge(v.getMaMon(), v, (a, b) -> pickPreferred(a, b, repeatPolicy));
        }

        int tongTCTichLuy = 0;
        int cpaGpaCredits = 0;
        double cpaWeighted = 0.0;
        int soMonTruot = 0;

        for (BangDiemSinhVienView rep : selectedBySubject.values()) {
            Double effTong = effectiveTotal(rep);
            int tc = rep.getSoTinChi() != null ? rep.getSoTinChi() : 0;
            boolean passed = effTong != null && effTong >= passScore;

            if (!passed) {
                soMonTruot++;
            } else {
                tongTCTichLuy += tc;
            }

            if (!Boolean.FALSE.equals(rep.getTinhVaoGPA())) {
                Double eff4 = effectiveGpa4(rep, effTong);
                if (eff4 != null && tc > 0) {
                    cpaGpaCredits += tc;
                    cpaWeighted += eff4 * tc;
                }
            }
        }

        Double cpa4 = round2(cpaGpaCredits > 0 ? cpaWeighted / cpaGpaCredits : null);
        String xepLoai = GradeScale.classifyStanding(cpa4);

        // 3. Cảnh báo học vụ theo Thông tư 08/2021 (kiểm tra trên kỳ có điểm gần nhất)
        List<String> warnings = new ArrayList<>();
        evaluatedTermsAsc.sort(Comparator.comparingInt((TermEval t) -> t.namHoc()).thenComparing(t -> t.maHK()));
        if (!evaluatedTermsAsc.isEmpty()) {
            TermEval latestTerm = evaluatedTermsAsc.get(evaluatedTermsAsc.size() - 1);
            int termCount = evaluatedTermsAsc.size();
            double minTermGpa = (termCount == 1) ? 0.8 : 1.0;
            if (latestTerm.gpa() != null && latestTerm.gpa() < minTermGpa) {
                warnings.add(String.format(Locale.US,
                        "GPA học kỳ gần nhất (%.2f) dưới ngưỡng %.2f", latestTerm.gpa(), minTermGpa));
            }
            double minCpaThreshold = switch (termCount) {
                case 1, 2 -> 1.2;
                case 3, 4 -> 1.4;
                case 5, 6 -> 1.6;
                default -> 1.8;
            };
            if (cpa4 != null && cpa4 < minCpaThreshold) {
                warnings.add(String.format(Locale.US,
                        "CPA tích lũy (%.2f) dưới ngưỡng %.2f cho năm học thứ %d", cpa4, minCpaThreshold, (termCount + 1) / 2));
            }
            if (latestTerm.gradedCredits() > 0
                    && latestTerm.failedCredits() * 2 > latestTerm.gradedCredits()) {
                warnings.add(String.format(
                        "Số tín chỉ không đạt kỳ gần nhất (%d/%d TC) vượt quá 50%%",
                        latestTerm.failedCredits(), latestTerm.gradedCredits()));
            }
        }

        return new EvaluationResult(
                byTerm,
                tongTCTichLuy,
                cpa4,
                xepLoai,
                soMonTruot,
                coCamThi,
                !warnings.isEmpty(),
                List.copyOf(warnings),
                termSummaries
        );
    }

    private BangDiemSinhVienView pickPreferred(BangDiemSinhVienView a,
                                               BangDiemSinhVienView b,
                                               GpaRepeatPolicy policy) {
        if (policy == GpaRepeatPolicy.LATEST) {
            return compareChronological(a, b) >= 0 ? a : b;
        }
        double scoreA = Objects.requireNonNullElse(effectiveTotal(a), -1.0);
        double scoreB = Objects.requireNonNullElse(effectiveTotal(b), -1.0);
        int cmp = Double.compare(scoreA, scoreB);
        if (cmp != 0) {
            return cmp > 0 ? a : b;
        }
        return compareChronological(a, b) >= 0 ? a : b;
    }

    private int compareChronological(BangDiemSinhVienView a, BangDiemSinhVienView b) {
        int yearCmp = Integer.compare(
                a.getNamHoc() != null ? a.getNamHoc() : 0,
                b.getNamHoc() != null ? b.getNamHoc() : 0
        );
        if (yearCmp != 0) {
            return yearCmp;
        }
        String hkA = a.getMaHK() != null ? a.getMaHK() : "";
        String hkB = b.getMaHK() != null ? b.getMaHK() : "";
        return hkA.compareTo(hkB);
    }

    private Double effectiveTotal(BangDiemSinhVienView v) {
        if (v.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI) {
            return 0.0;
        }
        return v.getDiemTong();
    }

    private Double effectiveGpa4(BangDiemSinhVienView v, Double effTong) {
        if (v.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI) {
            return 0.0;
        }
        if (v.getDiemHe4() != null) {
            return v.getDiemHe4();
        }
        return GradeScale.toGpa4(effTong);
    }

    private String formatTermLabel(BangDiemSinhVienView v) {
        if (v.getTenHK() != null && v.getNamHoc() != null) {
            return v.getTenHK() + " (" + v.getNamHoc() + ")";
        }
        return v.getMaHK() != null ? v.getMaHK() : "Khác";
    }

    private Double round2(Double val) {
        if (val == null) {
            return null;
        }
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record TermEval(String label, int namHoc, String maHK, Double gpa, int gradedCredits, int failedCredits) {
    }

    private record EvaluationResult(
            Map<String, List<BangDiemSinhVienView>> byTerm,
            int tongTCTichLuy,
            Double cpa4,
            String xepLoai,
            int soMonTruot,
            boolean coCamThi,
            boolean canhBaoHocVu,
            List<String> lyDoCanhBao,
            Map<String, TermTranscriptSummary> termSummaries
    ) {
    }
}
