package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LopHanhChinhForm {

    @NotBlank(message = "{lophc.maLopHC.required}")
    @Size(max = 20, message = "{lophc.maLopHC.size}")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "{common.code.pattern}")
    private String maLopHC;

    @NotBlank(message = "{lophc.tenLopHC.required}")
    @Size(max = 100, message = "{lophc.tenLopHC.size}")
    private String tenLopHC;

    @NotBlank(message = "{lophc.khoaNhapHoc.required}")
    private String maKhoaHoc;

    @NotBlank(message = "{lophc.nganhHoc.required}")
    private String maNganh;

    @NotBlank(message = "{lophc.covan.required}")
    private String maCoVan;
}
