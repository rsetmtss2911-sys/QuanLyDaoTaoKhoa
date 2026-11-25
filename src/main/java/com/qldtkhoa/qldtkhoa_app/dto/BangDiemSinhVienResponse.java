package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;

import java.util.List;
import java.util.Map;

public record BangDiemSinhVienResponse(
        Map<String, List<BangDiemSinhVienView>> byTerm,
        int tongTCTichLuy,
        Double cpa4,
        String xepLoai
) {}
