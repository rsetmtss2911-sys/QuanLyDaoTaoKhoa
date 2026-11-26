package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonHocForm {

    @NotBlank(message = "{monhoc.maMon.required}")
    @Size(max = 20, message = "{monhoc.maMon.size}")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maMon;

    @NotBlank(message = "{monhoc.tenMon.required}")
    @Size(max = 100, message = "{monhoc.tenMon.size}")
    private String tenMon;

    @NotNull(message = "{monhoc.soTinChi.required}")
    @Min(value = 1, message = "{monhoc.soTinChi.min}")
    @Max(value = 10, message = "{monhoc.soTinChi.max}")
    private Integer soTinChi = 3;

    @NotNull(message = "{monhoc.tinChiLT.required}")
    @Min(value = 0, message = "{monhoc.tinChiLT.min}")
    private Integer tinChiLT = 2;

    @NotNull(message = "{monhoc.tinChiTH.required}")
    @Min(value = 0, message = "{monhoc.tinChiTH.min}")
    private Integer tinChiTH = 1;

    @NotNull(message = "{monhoc.trongSoQT.required}")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double trongSoQT = 0.2;

    @NotNull(message = "{monhoc.trongSoGK.required}")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double trongSoGK = 0.3;

    @NotNull(message = "{monhoc.trongSoCK.required}")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double trongSoCK = 0.5;

    @NotBlank(message = "{monhoc.boMon.required}")
    private String maBoMon;
}
