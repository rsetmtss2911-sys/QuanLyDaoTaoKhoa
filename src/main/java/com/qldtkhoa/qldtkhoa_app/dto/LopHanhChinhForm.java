package com.qldtkhoa.qldtkhoa_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LopHanhChinhForm {

    @NotBlank(message = "{lophc.maLopHC.required}")
    @Size(max = 20, message = "{lophc.maLopHC.size}")
    private String maLopHC;

    @NotBlank(message = "{lophc.tenLopHC.required}")
    @Size(max = 100, message = "{lophc.tenLopHC.size}")
    private String tenLopHC;

    @NotBlank(message = "{lophc.khoaNhapHoc.required}")
    private String maKhoaHoc;

    @NotBlank(message = "{lophc.nganhHoc.required}")
    private String maNganh;

    private String maCoVan; // Mã nhân sự của giảng viên cố vấn (nullable)
}
