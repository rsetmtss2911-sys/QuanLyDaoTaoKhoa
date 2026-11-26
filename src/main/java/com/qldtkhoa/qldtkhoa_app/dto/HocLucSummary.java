package com.qldtkhoa.qldtkhoa_app.dto;

import java.util.List;
import java.util.Map;

public record HocLucSummary(
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
